import urllib.request
import json
import sqlite3
import os
import gzip
import time

def build_database():
    start_time = time.time()
    os.makedirs("app/src/main/assets/databases", exist_ok=True)
    temp_db_path = "/tmp/hadiths_sunni_prebuild.db"
    if os.path.exists(temp_db_path):
        os.remove(temp_db_path)

    print("Connecting to temporary SQLite database...")
    conn = sqlite3.connect(temp_db_path)
    cur = conn.cursor()

    # Room-compatible schema
    cur.execute("""
    CREATE TABLE IF NOT EXISTS `hadiths` (
        `id` TEXT NOT NULL,
        `bookKey` TEXT NOT NULL,
        `book` TEXT NOT NULL,
        `hadithNumber` TEXT NOT NULL,
        `chapter` TEXT NOT NULL,
        `narrator` TEXT NOT NULL,
        `arabicText` TEXT NOT NULL,
        `urduTranslation` TEXT NOT NULL,
        `englishTranslation` TEXT NOT NULL,
        `grade` TEXT NOT NULL,
        `reference` TEXT NOT NULL,
        `isBundled` INTEGER NOT NULL,
        PRIMARY KEY(`id`)
    )
    """)
    cur.execute("CREATE INDEX IF NOT EXISTS `index_hadiths_bookKey` ON `hadiths` (`bookKey`)")
    cur.execute("CREATE INDEX IF NOT EXISTS `index_hadiths_hadithNumber` ON `hadiths` (`hadithNumber`)")
    cur.execute("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
    cur.execute("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '2abebc8881ca3a9babcc212f98d6ee2b')")

    def fetch_and_parse_book(book_key, book_title, ara_url, eng_url, urd_url):
        print(f"Downloading {book_title} (Arabic, Urdu, English)...")
        ara_raw = urllib.request.urlopen(ara_url).read()
        eng_raw = urllib.request.urlopen(eng_url).read()
        urd_raw = urllib.request.urlopen(urd_url).read()

        ara_data = json.loads(ara_raw)
        eng_data = json.loads(eng_raw)
        urd_data = json.loads(urd_raw)

        eng_map = {h['hadithnumber']: h.get('text', '') for h in eng_data.get('hadiths', [])}
        urd_map = {h['hadithnumber']: h.get('text', '') for h in urd_data.get('hadiths', [])}
        sections = eng_data.get('metadata', {}).get('section', {})

        records = []
        for h in ara_data.get('hadiths', []):
            num = h['hadithnumber']
            ref = h.get('reference', {})
            book_num = str(ref.get('book', '1'))
            hadith_num_in_book = str(ref.get('hadith', num))
            chapter = sections.get(book_num, 'Sunnah & Virtues')

            eng = eng_map.get(num, '').strip()
            urd = urd_map.get(num, '').strip()
            ara = h.get('text', '').strip()

            narrator = 'Prophetic Sunnah (ﷺ)'
            if eng:
                idx = eng.find(':')
                if 5 <= idx <= 80 and (eng.startswith('Narrated') or eng.startswith('On the authority')):
                    narrator = eng[:idx].strip()

            reference = f"{book_title} {num}, Book {book_num}, Hadith {hadith_num_in_book}"
            records.append((
                f"{book_key}_{num}",
                book_key,
                book_title,
                str(num),
                chapter,
                narrator,
                ara,
                urd,
                eng,
                "Sahih (صحیح)",
                reference,
                1 # isBundled
            ))
        print(f"Parsed {len(records)} hadiths for {book_title}.")
        return records

    # 1. Sahih al-Bukhari
    bukhari_records = fetch_and_parse_book(
        "bukhari",
        "Sahih al-Bukhari",
        "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/ara-bukhari.min.json",
        "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/eng-bukhari.min.json",
        "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/urd-bukhari.min.json"
    )

    # 2. Sahih Muslim
    muslim_records = fetch_and_parse_book(
        "muslim",
        "Sahih Muslim",
        "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/ara-muslim.min.json",
        "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/eng-muslim.min.json",
        "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/urd-muslim.min.json"
    )

    all_records = bukhari_records + muslim_records
    print(f"Inserting {len(all_records)} total records into SQLite...")
    cur.executemany("INSERT INTO hadiths VALUES (?,?,?,?,?,?,?,?,?,?,?,?)", all_records)
    conn.commit()
    conn.close()

    raw_mb = os.path.getsize(temp_db_path) / (1024 * 1024)
    print(f"SQLite DB built successfully: {raw_mb:.2f} MB")

    gz_output = "app/src/main/assets/databases/sahihain_library.db.gz"
    print(f"Compressing into {gz_output}...")
    with open(temp_db_path, "rb") as f_in, gzip.open(gz_output, "wb", compresslevel=9) as f_out:
        while True:
            chunk = f_in.read(128 * 1024)
            if not chunk:
                break
            f_out.write(chunk)

    gz_mb = os.path.getsize(gz_output) / (1024 * 1024)
    print(f"GZipped asset ready: {gz_mb:.2f} MB in {time.time() - start_time:.2f} seconds!")

if __name__ == "__main__":
    build_database()
