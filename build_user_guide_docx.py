import zipfile
import os
import io

def generate_docx():
    # We will build a valid OOXML .docx file directly using Python standard libraries (zipfile + xml)
    # The document has rich formatting, tables, callout boxes, headers, footers, page numbering, etc.
    
    # Read existing logo
    logo_path = "app/src/main/res/drawable/app_logo.png"
    logo_bytes = b""
    if os.path.exists(logo_path):
        with open(logo_path, "rb") as f:
            logo_bytes = f.read()

    # Content Types XML
    content_types = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Default Extension="png" ContentType="image/png"/>
  <Default Extension="jpeg" ContentType="image/jpeg"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
  <Override PartName="/word/header1.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.header+xml"/>
  <Override PartName="/word/footer1.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.footer+xml"/>
</Types>"""

    # Package Relationships XML
    rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""

    # Document Relationships XML
    doc_rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/header" Target="header1.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer" Target="footer1.xml"/>
  <Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="media/image1.png"/>
</Relationships>"""

    # Styles XML
    styles = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:docDefaults>
    <w:rPrDefault>
      <w:rPr>
        <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:cs="Calibri"/>
        <w:sz w:val="22"/>
        <w:color w:val="262626"/>
      </w:rPr>
    </w:rPrDefault>
    <w:pPrDefault>
      <w:pPr>
        <w:spacing w:line="276" w:lineRule="auto" w:after="160"/>
      </w:pPr>
    </w:pPrDefault>
  </w:docDefaults>
  
  <w:style w:type="paragraph" w:styleId="Normal">
    <w:name w:val="Normal"/>
    <w:qFormat/>
  </w:style>

  <w:style w:type="paragraph" w:styleId="Title">
    <w:name w:val="Title"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr>
      <w:spacing w:before="360" w:after="120"/>
      <w:jc w:val="center"/>
    </w:pPr>
    <w:rPr>
      <w:rFonts w:ascii="Calibri Light" w:hAnsi="Calibri Light"/>
      <w:b/>
      <w:sz w:val="56"/>
      <w:color w:val="0B3B24"/>
    </w:rPr>
  </w:style>

  <w:style w:type="paragraph" w:styleId="Subtitle">
    <w:name w:val="Subtitle"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr>
      <w:spacing w:before="0" w:after="300"/>
      <w:jc w:val="center"/>
    </w:pPr>
    <w:rPr>
      <w:sz w:val="26"/>
      <w:color w:val="D4AF37"/>
    </w:rPr>
  </w:style>

  <w:style w:type="paragraph" w:styleId="Heading1">
    <w:name w:val="heading 1"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr>
      <w:spacing w:before="400" w:after="140"/>
      <w:pBdr>
        <w:bottom w:val="single" w:sz="18" w:space="4" w:color="0B3B24"/>
      </w:pBdr>
    </w:pPr>
    <w:rPr>
      <w:rFonts w:ascii="Calibri Light" w:hAnsi="Calibri Light"/>
      <w:b/>
      <w:sz w:val="34"/>
      <w:color w:val="0B3B24"/>
    </w:rPr>
  </w:style>

  <w:style w:type="paragraph" w:styleId="Heading2">
    <w:name w:val="heading 2"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr>
      <w:spacing w:before="240" w:after="100"/>
    </w:pPr>
    <w:rPr>
      <w:b/>
      <w:sz w:val="26"/>
      <w:color w:val="145A32"/>
    </w:rPr>
  </w:style>

  <w:style w:type="paragraph" w:styleId="Heading3">
    <w:name w:val="heading 3"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr>
      <w:spacing w:before="160" w:after="60"/>
    </w:pPr>
    <w:rPr>
      <w:b/>
      <w:sz w:val="23"/>
      <w:color w:val="B7950B"/>
    </w:rPr>
  </w:style>
</w:styles>"""

    # Header XML
    header = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:hdr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:p>
    <w:pPr>
      <w:pBdr>
        <w:bottom w:val="single" w:sz="6" w:space="4" w:color="D4AF37"/>
      </w:pBdr>
      <w:jc w:val="right"/>
    </w:pPr>
    <w:r>
      <w:rPr>
        <w:sz w:val="18"/>
        <w:color w:val="7F8C8D"/>
      </w:rPr>
      <w:t>Alnoor Islami Trust • Complete Official Member User Guide</w:t>
    </w:r>
  </w:p>
</w:hdr>"""

    # Footer XML
    footer = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:ftr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:p>
    <w:pPr>
      <w:pBdr>
        <w:top w:val="single" w:sz="6" w:space="4" w:color="D4AF37"/>
      </w:pBdr>
      <w:tabs>
        <w:tab w:val="center" w:pos="4500"/>
        <w:tab w:val="right" w:pos="9000"/>
      </w:tabs>
    </w:pPr>
    <w:r>
      <w:rPr>
        <w:sz w:val="18"/>
        <w:color w:val="7F8C8D"/>
      </w:rPr>
      <w:t>Support: +92-333-2434114 | info@alnoorislami.pk</w:t>
    </w:r>
    <w:r>
      <w:tab/>
    </w:r>
    <w:r>
      <w:rPr>
        <w:sz w:val="18"/>
        <w:color w:val="7F8C8D"/>
      </w:rPr>
      <w:t>Page </w:t>
    </w:r>
    <w:fldSimple w:instr="PAGE"/>
    <w:r>
      <w:rPr>
        <w:sz w:val="18"/>
        <w:color w:val="7F8C8D"/>
      </w:rPr>
      <w:t> of </w:t>
    </w:r>
    <w:fldSimple w:instr="NUMPAGES"/>
  </w:p>
</w:ftr>"""

    # Helper XML generation functions
    def p(text="", style=None, bold=False, italic=False, color=None, size=None, align=None, space_after=140, space_before=0):
        xml = ['<w:p>']
        pPr = ['<w:pPr>']
        if style:
            pPr.append(f'<w:pStyle w:val="{style}"/>')
        if align:
            pPr.append(f'<w:jc w:val="{align}"/>')
        pPr.append(f'<w:spacing w:before="{space_before}" w:after="{space_after}"/>')
        pPr.append('</w:pPr>')
        xml.append(''.join(pPr))
        
        if text:
            xml.append('<w:r>')
            rPr = ['<w:rPr>']
            if bold:
                rPr.append('<w:b/>')
            if italic:
                rPr.append('<w:i/>')
            if color:
                rPr.append(f'<w:color w:val="{color}"/>')
            if size:
                rPr.append(f'<w:sz w:val="{size}"/>')
            rPr.append('</w:rPr>')
            xml.append(''.join(rPr))
            # Escape XML characters
            clean_text = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            xml.append(f'<w:t xml:space="preserve">{clean_text}</w:t>')
            xml.append('</w:r>')
        xml.append('</w:p>')
        return ''.join(xml)

    def heading1(text):
        return p(text, style="Heading1", space_before=360, space_after=140)

    def heading2(text):
        return p(text, style="Heading2", space_before=240, space_after=100)

    def heading3(text):
        return p(text, style="Heading3", space_before=160, space_after=60)

    def bullet(title, text):
        xml = ['<w:p>']
        xml.append('<w:pPr><w:ind w:left="400" w:hanging="240"/><w:spacing w:after="80"/></w:pPr>')
        xml.append('<w:r><w:rPr><w:color w:val="D4AF37"/><w:b/></w:rPr><w:t>❖ </w:t></w:r>')
        if title:
            clean_title = title.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            xml.append(f'<w:r><w:rPr><w:b/><w:color w:val="0B3B24"/></w:rPr><w:t xml:space="preserve">{clean_title}: </w:t></w:r>')
        clean_text = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        xml.append(f'<w:r><w:t xml:space="preserve">{clean_text}</w:t></w:r>')
        xml.append('</w:p>')
        return ''.join(xml)

    def callout_box(title, text, icon="💡", bg_color="EAFAF1", border_color="1E8449"):
        xml = ['<w:tbl>']
        xml.append('<w:tblPr>')
        xml.append('<w:tblW w:w="9200" w:type="dxa"/>')
        xml.append(f'<w:tblBorders><w:left w:val="single" w:sz="36" w:space="8" w:color="{border_color}"/>')
        xml.append('<w:top w:val="none"/><w:right w:val="none"/><w:bottom w:val="none"/></w:tblBorders>')
        xml.append('</w:tblPr>')
        xml.append('<w:tr>')
        xml.append(f'<w:tc><w:tcPr><w:tcW w:w="9200" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="{bg_color}"/><w:tcMar><w:top w:w="160"/><w:left w:w="240"/><w:bottom w:w="160"/><w:right w:w="240"/></w:tcMar></w:tcPr>')
        clean_title = title.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        clean_text = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        xml.append(f'<w:p><w:pPr><w:spacing w:after="60"/></w:pPr><w:r><w:rPr><w:b/><w:color w:val="{border_color}"/><w:sz w:val="23"/></w:rPr><w:t xml:space="preserve">{icon} {clean_title}</w:t></w:r></w:p>')
        xml.append(f'<w:p><w:pPr><w:spacing w:after="0"/></w:pPr><w:r><w:rPr><w:sz w:val="21"/><w:color w:val="333333"/></w:rPr><w:t xml:space="preserve">{clean_text}</w:t></w:r></w:p>')
        xml.append('</w:tc></w:tr></w:tbl>')
        xml.append(p("", space_after=120))
        return ''.join(xml)

    def card_spec_table(key_name, title, subtitle, purpose, user_actions, features):
        xml = ['<w:tbl>']
        xml.append('<w:tblPr>')
        xml.append('<w:tblW w:w="9200" w:type="dxa"/>')
        xml.append('<w:tblBorders>')
        xml.append('<w:top w:val="single" w:sz="8" w:color="D4AF37"/>')
        xml.append('<w:left w:val="single" w:sz="8" w:color="D4AF37"/>')
        xml.append('<w:bottom w:val="single" w:sz="8" w:color="D4AF37"/>')
        xml.append('<w:right w:val="single" w:sz="8" w:color="D4AF37"/>')
        xml.append('<w:insideH w:val="single" w:sz="4" w:color="E0E0E0"/>')
        xml.append('<w:insideV w:val="single" w:sz="4" w:color="E0E0E0"/>')
        xml.append('</w:tblBorders>')
        xml.append('</w:tblPr>')

        # Row 1: Header (Title & Card Badge)
        xml.append('<w:tr>')
        xml.append('<w:tc><w:tcPr><w:gridSpan w:val="2"/><w:shd w:val="clear" w:color="auto" w:fill="0B3B24"/><w:tcMar><w:top w:w="160"/><w:left w:w="200"/><w:bottom w:w="160"/><w:right w:w="200"/></w:tcMar></w:tcPr>')
        xml.append(f'<w:p><w:pPr><w:spacing w:after="0"/></w:pPr><w:r><w:rPr><w:b/><w:color w:val="FFFFFF"/><w:sz w:val="25"/></w:rPr><w:t xml:space="preserve">📌 {title}</w:t></w:r><w:r><w:rPr><w:color w:val="D4AF37"/><w:sz w:val="20"/></w:rPr><w:t xml:space="preserve">  [Key: {key_name}]</w:t></w:r></w:p>')
        xml.append('</w:tc></w:tr>')

        # Helper for key-value row
        def row(label, val):
            return f'''<w:tr>
<w:tc><w:tcPr><w:tcW w:w="2500" w:type="dxa"/><w:shd w:val="clear" w:color="auto" w:fill="F4F6F7"/><w:tcMar><w:top w:w="120"/><w:left w:w="160"/><w:bottom w:w="120"/><w:right w:w="160"/></w:tcMar></w:tcPr>
<w:p><w:pPr><w:spacing w:after="0"/></w:pPr><w:r><w:rPr><w:b/><w:color w:val="0B3B24"/><w:sz w:val="20"/></w:rPr><w:t>{label}</w:t></w:r></w:p></w:tc>
<w:tc><w:tcPr><w:tcW w:w="6700" w:type="dxa"/><w:tcMar><w:top w:w="120"/><w:left w:w="160"/><w:bottom w:w="120"/><w:right w:w="160"/></w:tcMar></w:tcPr>
<w:p><w:pPr><w:spacing w:after="0"/></w:pPr><w:r><w:rPr><w:sz w:val="20"/><w:color w:val="2C3E50"/></w:rPr><w:t xml:space="preserve">{val.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")}</w:t></w:r></w:p></w:tc>
</w:tr>'''

        xml.append(row("Dashboard Subtitle", subtitle))
        xml.append(row("Card Purpose", purpose))
        xml.append(row("How to Use (Tap Action)", user_actions))
        xml.append(row("Key Features for Members", features))

        xml.append('</w:tbl>')
        xml.append(p("", space_after=160))
        return ''.join(xml)

    # Logo drawing XML for Title page
    logo_drawing = """<w:p>
<w:pPr><w:jc w:val="center"/><w:spacing w:before="300" w:after="240"/></w:pPr>
<w:r>
  <w:drawing>
    <wp:inline distT="0" distB="0" distL="0" distR="0" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing">
      <wp:extent cx="1371600" cy="1371600"/>
      <wp:docPr id="1" name="App Logo"/>
      <wp:cNvGraphicFramePr>
        <a:graphicFrameLocks noChangeAspect="1" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"/>
      </wp:cNvGraphicFramePr>
      <a:graphic xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
        <a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
          <pic:pic xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">
            <pic:nvPicPr>
              <pic:cNvPr id="0" name="app_logo.png"/>
              <pic:cNvPicPr/>
            </pic:nvPicPr>
            <pic:blipFill>
              <a:blip r:embed="rId4" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"/>
              <a:stretch><a:fillRect/></a:stretch>
            </pic:blipFill>
            <pic:spPr>
              <a:xfrm><a:off x="0" y="0"/><a:ext cx="1371600" cy="1371600"/></a:xfrm>
              <a:prstGeom prst="roundRect"><a:avLst><a:gd name="adj" fmla="val 12000"/></a:avLst></a:prstGeom>
            </pic:spPr>
          </pic:pic>
        </a:graphicData>
      </a:graphic>
    </wp:inline>
  </w:drawing>
</w:r>
</w:p>"""

    # Assemble complete Document XML body
    body_parts = []

    # Title & Front Matter
    body_parts.append(logo_drawing)
    body_parts.append(p("AL NOOR ISLAMI", style="Title", bold=True, size=52, color="0B3B24", align="center", space_before=120, space_after=80))
    body_parts.append(p("Al Noor International Trust", bold=True, size=30, color="D4AF37", align="center", space_after=60))
    body_parts.append(p("OFFICIAL COMPLETE MEMBER USER MANUAL", bold=True, size=26, color="1B4D3E", align="center", space_after=200))
    body_parts.append(p("A Step-by-Step Practical Guide for Installation, Registration, Action Cards, Daily Worship, Quran Reader, Mahafil Broadcasts, and Support Troubleshooting", italic=True, size=21, color="566573", align="center", space_after=360))

    body_parts.append(callout_box(
        "Important Note for Members",
        "This official handbook is exclusively written for general congregation members, attendees of Mahafil, and readers worldwide. It covers all everyday member functions, daily recitations, live streaming, offline audio listening, and self-help troubleshooting. Administrative management controls are maintained in a separate manual.",
        icon="📖",
        bg_color="E8F8F5",
        border_color="117A65"
    ))

    body_parts.append(p("", space_after=200))

    # TABLE OF CONTENTS
    body_parts.append(heading1("Table of Contents"))
    body_parts.append(bullet("Chapter 1", "Introduction to Alnoor Islami Application"))
    body_parts.append(bullet("Chapter 2", "System Requirements & Installation Guide (APK & Play Store)"))
    body_parts.append(bullet("Chapter 3", "Account Creation & Registration (Email, Phone, Google)"))
    body_parts.append(bullet("Chapter 4", "Login, Session Security & Password Recovery"))
    body_parts.append(bullet("Chapter 5", "Navigating the Home Dashboard & Layout"))
    body_parts.append(bullet("Chapter 6", "Comprehensive Breakdown of All 12 Action Cards & Modules"))
    body_parts.append(bullet("Chapter 7", "Worship Tools: Darood Counter, Quran Reader & Prayer Times"))
    body_parts.append(bullet("Chapter 8", "Multimedia: Live YouTube Broadcasts, Archive & Audio Player"))
    body_parts.append(bullet("Chapter 9", "In-App Updates (Mandatory & In-Place One-Tap APK Updates)"))
    body_parts.append(bullet("Chapter 10", "Member Troubleshooting, FAQ & Contact Support"))

    body_parts.append(p("", space_after=300))

    # CHAPTER 1
    body_parts.append(heading1("Chapter 1: Introduction to Alnoor Islami"))
    body_parts.append(p("The Alnoor Islami mobile application is developed under the patronage of Al Noor International Trust. Designed as a digital sanctuary for Muslims across the globe, it unites authentic spiritual enrichment, scholarly wisdom, Quranic study, collective Darood Shareef recitation, live Mehfil-e-Zikr broadcasts, and organizational announcements into a single, cohesive, ad-free mobile experience."))
    body_parts.append(p("Key Pillars of the Experience:", bold=True, color="0B3B24"))
    body_parts.append(bullet("Authentic Islamic Heritage", "Access genuine Bukhari & Muslim Hadith collections, precise Prayer timings, and high-definition 16-line / Indo-Pak Quran-e-Pak."))
    bullet("Collective Spiritual Connectivity", "Submit and monitor daily Darood Shareef recitations contributing to millions recited globally by the congregation.")
    bullet("Live & Recorded Mahafil", "Join live broadcasts of weekly and monthly gatherings, or listen to high-quality audio Bayanat even when your screen is turned off.")
    bullet("Privacy & Purity", "Completely free of distracting third-party advertisements, intrusive trackers, or commercial monetization.")

    # CHAPTER 2
    body_parts.append(heading1("Chapter 2: System Requirements & Installation"))
    body_parts.append(p("The Alnoor Islami application is optimized for modern Android smartphones and tablets running Android 7.0 (Nougat) up to Android 15+."))
    body_parts.append(heading2("2.1 Minimum & Recommended Hardware Specs"))
    body_parts.append(bullet("Operating System", "Android 7.0 (API Level 24) or higher."))
    body_parts.append(bullet("RAM", "2 GB minimum (3 GB+ recommended for seamless background audio playback)."))
    body_parts.append(bullet("Free Storage", "Approximately 60 MB for initial installation + 150 MB buffer for offline Quran pages and cached Bayanat."))
    body_parts.append(bullet("Internet Connectivity", "Wi-Fi or Mobile Data (4G/5G) required for initial registration, live streams, and syncing daily counts."))

    body_parts.append(heading2("2.2 Installing the APK Package directly on Android"))
    body_parts.append(p("If you are receiving the official APK directly via WhatsApp, Telegram, or the Alnoor website, follow these simple installation steps:"))
    body_parts.append(bullet("Step 1", "Tap on the received 'alnoor-islami.apk' file on your phone."))
    body_parts.append(bullet("Step 2", "If prompted with 'Install unknown apps' or 'Allow from this source', tap Settings and toggle the switch to ALLOW."))
    body_parts.append(bullet("Step 3", "Return to the installer dialog and tap 'Install'."))
    body_parts.append(bullet("Step 4", "Once installation finishes, tap 'Open' to launch the app."))

    body_parts.append(callout_box(
        "Android 13 & 14 Notification Permission",
        "Upon your first launch, your phone will ask: 'Allow Alnoor Islami to send you notifications?'. Please tap ALLOW. This is essential for receiving Prayer Adhan reminders, live broadcast notifications, and urgent Trust announcements.",
        icon="🔔",
        bg_color="FEF9E7",
        border_color="D4AC0D"
    ))

    # CHAPTER 3
    body_parts.append(heading1("Chapter 3: Account Creation & Registration"))
    body_parts.append(p("Registering as an official member connects your device to your personal Darood counter history and allows administrators to issue verified member certificates."))
    body_parts.append(heading2("3.1 Available Sign-up Methods"))
    body_parts.append(bullet("1. Google One-Tap Sign-In (Recommended)", "Fastest and most secure. Simply tap 'Continue with Google' to instantly authenticate without remembering an extra password."))
    body_parts.append(bullet("2. Email & Password Registration", "Tap 'Create Account', enter your Full Name, Active Email address, and choose a secure password (minimum 6 characters with letters and numbers)."))
    body_parts.append(bullet("3. Phone Number / WhatsApp Sign-Up", "Enter your mobile phone number with the international country code (e.g., +92 for Pakistan, +44 for UK)."))

    body_parts.append(heading2("3.2 Profile Details"))
    body_parts.append(p("During registration, you are encouraged to provide your accurate City and Country. This ensures that:"))
    body_parts.append(bullet("Prayer Times Precision", "Your prayer schedule and Qibla compass automatically calibrate to your geographical coordinates."))
    body_parts.append(bullet("Regional Event Notices", "You receive announcements tailored to your regional chapter or timezone."))

    # CHAPTER 4
    body_parts.append(heading1("Chapter 4: Login, Session & Password Recovery"))
    body_parts.append(p("Once registered, the app keeps your session securely active on your personal phone so you do not have to type your password repeatedly."))
    body_parts.append(heading2("4.1 Logging In"))
    body_parts.append(bullet("Step 1", "Launch the app and enter your registered Email address."))
    body_parts.append(bullet("Step 2", "Type your password in the secure password field (tap the eye icon to verify spelling)."))
    body_parts.append(bullet("Step 3", "Tap the emerald 'Sign In' button."))

    body_parts.append(heading2("4.2 Password Recovery ('Forgot Password?')"))
    body_parts.append(bullet("Step 1", "If you cannot recall your password, tap the gold 'Forgot Password?' link on the login screen."))
    body_parts.append(bullet("Step 2", "Enter your registered email address and tap 'Send Reset Link'."))
    body_parts.append(bullet("Step 3", "Check your email inbox (and Spam/Junk folder) for a secure password reset link from Alnoor Trust."))
    body_parts.append(bullet("Step 4", "Click the link in your email, type a new password, and then return to the app to log in."))

    # CHAPTER 5
    body_parts.append(heading1("Chapter 5: Navigating the Home Dashboard"))
    body_parts.append(p("The home dashboard is designed with an intuitive, single-column vertical flow framed in deep Islamic emerald green and royal gold accents."))
    body_parts.append(heading2("5.1 Top Bar & Header"))
    body_parts.append(bullet("Official Brand Emblem", "Displays the Alnoor Islami Trust crest on the upper left."))
    body_parts.append(bullet("Active Member Profile", "Shows your registered name and membership badge."))
    body_parts.append(bullet("Bismillah Calligraphy Header", "A beautiful gold-inscribed recitation of 'Bismillahir Rahmanir Rahim' blessing every session."))

    body_parts.append(heading2("5.2 Action Cards Section"))
    body_parts.append(p("Beneath the top banners, the 'APPLICATION ACTIONS' section provides quick, direct access to the app's 12 primary spiritual modules. Each card is custom-tailored for maximum legibility and 1-tap responsiveness."))

    # CHAPTER 6
    body_parts.append(heading1("Chapter 6: Comprehensive Breakdown of All Action Cards"))
    body_parts.append(p("Here is the detailed reference guide for every action card available on your dashboard:"))

    # Card 1: ALNOOR_CHANNEL
    body_parts.append(card_spec_table(
        key_name="ALNOOR_CHANNEL",
        title="Alnoor Islami Channel",
        subtitle="Official YouTube Channel, recorded Mahafil archive.",
        purpose="Provides a unified multimedia portal directly linking members to the Trust's official YouTube broadcast channels, recorded Bayanat archives, and live Mehfil streams.",
        user_actions="Tap 'Click to YouTube Channel' to open the complete video catalog in the YouTube app. Tap 'Click to view Live Scheduled Mehfil' to enter ongoing live broadcasts.",
        features="Deep-links directly into the official YouTube Android app for high-definition viewing. Gracefully falls back to browser if YouTube app is unavailable."
    ))

    # Card 2: DAROOD
    body_parts.append(card_spec_table(
        key_name="DAROOD",
        title="Darood Sharif Collection",
        subtitle="Send your daily Darood sharif recitation for overall collection.",
        purpose="Enables members worldwide to contribute to the global Alnoor Darood Shareef treasury. Tracks personal daily goals while uniting millions of salawat into the worldwide total.",
        user_actions="Tap the card to open the interactive Darood Counter. Use the giant haptic tap button or quick presets (+10, +33, +100) to log recitations, then tap 'Submit to Trust'.",
        features="Audible click feedback, vibration haptics, daily milestone celebrations, and offline caching that automatically synchronizes when you reconnect to Wi-Fi/data."
    ))

    # Card 3: HADITH
    body_parts.append(card_spec_table(
        key_name="HADITH",
        title="Daily Hadith Shareef (حدیث شریف)",
        subtitle="Authentic Bukhari & Muslim Hadith in Arabic, Urdu & English with card save & share.",
        purpose="Presents a verified daily Hadith with full Arabic text (Aa'raab), authentic Urdu translation, English translation, and book citation reference.",
        user_actions="Tap 'Read Hadith' to expand the full text inline. Tap 'Save 9:16 Image' to download a poster for WhatsApp Status. Tap 'Share' to post the text and image directly to contacts.",
        features="High-resolution 9:16 vertical image generator (1080x1920) formatted specifically for phone screens and WhatsApp statuses with zero text clipping."
    ))

    # Card 4: EVENTS
    body_parts.append(card_spec_table(
        key_name="EVENTS",
        title="Events & Mahafil Calendar",
        subtitle="Schedule of upcoming Bayanat, Shab-e-Baraat, Ramadan, and monthly gatherings.",
        purpose="Keeps the entire community informed about upcoming spiritual assemblies, Urs commemorations, weekly Halqa-e-Zikr, and special Islamic dates.",
        user_actions="Tap to view the interactive monthly calendar. Tap on any event to read the venue address, Google Maps directions, start time, and speaker details.",
        features="'Add to Phone Calendar' 1-tap sync button, countdown timers to major gatherings, and venue navigation maps."
    ))

    # Card 5: NOTICES
    body_parts.append(card_spec_table(
        key_name="NOTICES",
        title="Official Announcements & Notices",
        subtitle="Trust directives, moon sighting reports, and urgent community messages.",
        purpose="Serves as the verified, tamper-proof bulletin board of Alnoor International Trust directly from the leadership.",
        user_actions="Tap to view recent circulars in reverse chronological order. Unread notices display a prominent golden indicator dot.",
        features="Instant push notification alerts, shareable circular PDFs, and verified admin authorization seal on every announcement."
    ))

    # Card 6: QURAN
    body_parts.append(card_spec_table(
        key_name="QURAN",
        title="Quran-e-Pak (قرآن پاک)",
        subtitle="Read Holy Quran with 16-line page format, Urdu translation & last-read bookmark.",
        purpose="A distraction-free, sacred reading environment replicating the beloved 16-line South Asian / Indo-Pak printed Mushaf.",
        user_actions="Tap to resume directly from your saved ayah/page, or browse the Surah index (1 to 114) and Parah index (1 to 30).",
        features="Crisp vector typography, page curl / swipe gestures, dark night mode for eye comfort, and automatic bookmarking of your last read position."
    ))

    # Card 7: PRAYER
    body_parts.append(card_spec_table(
        key_name="PRAYER",
        title="Prayer Times & Qibla Compass",
        subtitle="Accurate Fajr, Dhuhr, Asr, Maghrib & Isha timings calibrated to your location.",
        purpose="Ensures you never miss a prayer anywhere in the world by computing exact astronomical solar angles according to your precise GPS coordinates.",
        user_actions="Tap to view the full prayer timetable for the day. Tap the 'Qibla' button to open the real-time gyro-stabilized Kaaba compass.",
        features="Configurable calculation juristic methods (Hanafi/Shafi'i), pre-Adhan silent alerts, and remaining time countdown to the next prayer."
    ))

    # Card 8: LIBRARY
    body_parts.append(card_spec_table(
        key_name="LIBRARY",
        title="Islamic Library & Publications",
        subtitle="Download authentic Islamic books, research papers, and Wazaif booklets in PDF.",
        purpose="A comprehensive digital repository of authentic Islamic literature, spiritual guides, and trust treatises available for instant offline study.",
        user_actions="Tap to browse publications by category (Tasawwuf, Fiqh, Seerah, Duas). Tap 'Download' to save any book for offline reading inside the built-in PDF viewer.",
        features="In-app PDF reader with zoom, page jump, continuous vertical scrolling, and search indexing."
    ))

    # Card 9: MEDIA
    body_parts.append(card_spec_table(
        key_name="MEDIA",
        title="Audio Bayanat & Media Archive",
        subtitle="Listen to inspiring lectures, Naats, and spiritual discourses with background playback.",
        purpose="Provides a curated audio library of Bayanat and recitations that you can listen to while commuting, working, or resting.",
        user_actions="Tap any audio track to begin streaming. Tap the download cloud icon to save tracks for listening without an internet connection.",
        features="Full background audio service with lock-screen media controls, Bluetooth auto-resume, sleep timer (15, 30, 60 mins), and playback speed control (1.0x, 1.25x, 1.5x)."
    ))

    # Card 10: GALLERY
    body_parts.append(card_spec_table(
        key_name="GALLERY",
        title="Photo Gallery & Historical Archives",
        subtitle="High-resolution pictures of Mahafil, construction projects, and trust welfare activities.",
        purpose="Showcases the visual history and welfare work of Alnoor Trust, including medical camps, ration distributions, and mosque developments.",
        user_actions="Tap to open the image grid. Tap any photograph for high-definition full-screen viewing with pinch-to-zoom.",
        features="Categorized albums, photo captions with dates and venues, and 1-tap download to device photo gallery."
    ))

    # Card 11: MESSAGES
    body_parts.append(card_spec_table(
        key_name="MESSAGES",
        title="Ask a Question / Scholar Inquiry",
        subtitle="Submit your spiritual, religious, or personal inquiry to the Trust scholars privately.",
        purpose="Provides a confidential, direct communication channel between community members and qualified scholars.",
        user_actions="Tap 'New Inquiry', select a subject category (Worship, Family, Spiritual Advice), type your question, and tap 'Send'.",
        features="End-to-end privacy, notifications when your answer is ready, and a personal archive of your past inquiries."
    ))

    # Card 12: SETTINGS
    body_parts.append(card_spec_table(
        key_name="SETTINGS",
        title="App Preferences & Card Customization",
        subtitle="Customize dashboard layout, theme color, language, and notification alerts.",
        purpose="Empowers each member to tailor the application's appearance and behavior to their personal spiritual routine.",
        user_actions="Tap to adjust Adhan alert tones, choose your preferred language (English/Urdu), or tap 'Customize Cards' to reorder dashboard cards.",
        features="Drag-and-drop card reordering, cache cleanup utility, font size sliders, and one-tap app version update check."
    ))

    # CHAPTER 7
    body_parts.append(heading1("Chapter 7: Worship & Spiritual Tools"))
    body_parts.append(p("The following step-by-step guides walk you through the core everyday worship modules:"))

    body_parts.append(heading2("7.1 Interactive Darood Counter"))
    body_parts.append(bullet("Single Tap Counting", "Tap the central emerald disc to increment your count by 1. A gentle vibration confirms each recitation."))
    bullet("Quick Multipliers", "Use the '+10', '+33', or '+100' pills to instantly register recitations completed on physical tasbih beads.")
    bullet("Submitting to Trust", "Tap 'Submit Recitation'. Your count is immediately pooled into the worldwide congregation total and reset for your next session.")

    body_parts.append(heading2("7.2 Quran-e-Pak Reader"))
    body_parts.append(bullet("Navigation Modes", "Switch between Surah View (ordered 1 to 114) and Parah View (Juz 1 to 30) using the top selector tabs."))
    bullet("Dual Display Options", "Choose between 16-Line South Asian Arabic text or Line-by-Line with Urdu translation.")
    bullet("Automatic Bookmarks", "When you close the app, your exact Ayah and Page are automatically remembered. Tap 'Resume Reading' on your next visit to return instantly.")

    body_parts.append(heading2("7.3 Prayer Times & Qibla Direction"))
    body_parts.append(bullet("Automatic Geolocation", "Upon opening, the app detects your city coordinates and calculates prayer times."))
    bullet("Calibrating the Qibla Compass", "Hold your phone flat and rotate it in a figure-8 motion to calibrate the internal magnetic sensors. Rotate your body until the golden compass needle points directly to the Holy Kaaba emblem.")

    # CHAPTER 8
    body_parts.append(heading1("Chapter 8: Live Broadcasts & Multimedia"))
    body_parts.append(heading2("8.1 Joining a Live Mehfil Broadcast"))
    body_parts.append(p("When an active gathering is broadcasting from the Markaz:"))
    body_parts.append(bullet("Visual Alert", "The Alnoor Islami Channel card highlights in red with a live indicator."))
    body_parts.append(bullet("1-Tap Join", "Tap 'Click to view Live Scheduled Mehfil'. The official YouTube broadcast will open automatically in highest available resolution."))

    body_parts.append(heading2("8.2 Background Audio Bayanat"))
    body_parts.append(p("You can listen to discourses with your phone screen turned off or while using other applications:"))
    body_parts.append(bullet("Step 1", "Go to the 'Audio Bayanat & Media Archive' card."))
    body_parts.append(bullet("Step 2", "Select any Bayan track and tap Play."))
    body_parts.append(bullet("Step 3", "You may now lock your phone screen or put it in your pocket. The audio continues playing smoothly."))
    body_parts.append(bullet("Step 4", "Control pause, resume, and skip directly from your Android lock screen or notification drawer."))

    # CHAPTER 9
    body_parts.append(heading1("Chapter 9: In-App Updates"))
    body_parts.append(p("To ensure you always have the latest features, security enhancements, and prayer timetable algorithms, the app features an automated in-app update system."))
    body_parts.append(heading2("9.1 The Modern Single-Action Update Flow"))
    body_parts.append(p("When a new version is released by the Trust, an update notice appears on your screen:"))
    body_parts.append(bullet("Single Tap Action", "Simply tap the prominent emerald button: 'Update Now (XX.X MB)'."))
    body_parts.append(bullet("Progress Bar", "A clean progress indicator shows download percentage in real-time."))
    body_parts.append(bullet("Automatic Verification & Install", "Once the file is downloaded and verified, the button changes to 'Install Update Now'. Tap it to refresh your app."))
    body_parts.append(bullet("Silent Cleanup", "The app automatically and silently removes any old, obsolete temporary APK files from your device storage to prevent wasted space."))

    # CHAPTER 10
    body_parts.append(heading1("Chapter 10: Troubleshooting & Support FAQ"))
    body_parts.append(p("If you encounter any unexpected issues, consult these quick solutions:"))

    body_parts.append(heading2("10.1 Common Questions & Quick Fixes"))
    body_parts.append(callout_box(
        "Issue: Prayer times are showing incorrect hours",
        "Solution: Ensure Location (GPS) permission is granted to the app. In App Settings -> Prayer Settings, verify your calculation method (e.g., Karachi / University of Islamic Sciences for South Asia) and ensure Day Light Savings (DST) is set appropriately for your country.",
        icon="⏰",
        bg_color="FDEDEC",
        border_color="C0392B"
    ))

    body_parts.append(callout_box(
        "Issue: App says 'Download failed' during an update",
        "Solution: Check your Wi-Fi or mobile data signal. If storage is full, free up at least 50 MB on your phone. Tap the 'Having trouble?' accordion on the update screen to open the direct browser download link as a fallback.",
        icon="📶",
        bg_color="FEF9E7",
        border_color="D4AC0D"
    ))

    body_parts.append(callout_box(
        "Issue: Audio stops playing when phone screen locks",
        "Solution: On some phone brands (Xiaomi, Huawei, Samsung), aggressive battery savers may pause background apps. Go to Phone Settings -> Apps -> Alnoor Islami -> Battery -> Set to 'Unrestricted' or 'Do not optimize'.",
        icon="🔋",
        bg_color="E8F8F5",
        border_color="117A65"
    ))

    body_parts.append(callout_box(
        "Issue: I am not receiving Adhan or broadcast notifications",
        "Solution: Check that Notifications are enabled in your phone's Settings -> Apps -> Alnoor Islami -> Notifications -> Allow all notifications. Also check that your phone is not in 'Do Not Disturb' (DND) mode.",
        icon="🔕",
        bg_color="F4F6F7",
        border_color="7F8C8D"
    ))

    body_parts.append(heading2("10.2 Official Helpdesk Contacts"))
    body_parts.append(p("If your query remains unresolved, our dedicated volunteer support team is ready to assist you:"))
    body_parts.append(bullet("Official WhatsApp Helpdesk", "+92-333-2434114"))
    bullet("Official Support Email", "info@alnoorislami.pk")
    bullet("Official Website", "https://alnoorislami.pk")
    bullet("Markaz Address", "Al Noor International Trust, Karachi, Pakistan")

    # Document closure
    body_parts.append(p("", space_after=300))
    body_parts.append(p("May Allah Almighty accept all your good deeds, recitations, and prayers.", italic=True, bold=True, color="0B3B24", align="center", size=24))
    body_parts.append(p("AL NOOR INTERNATIONAL TRUST • ALL RIGHTS RESERVED", bold=True, color="D4AF37", align="center", size=18))

    # Assemble Document XML
    doc_xml = f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
            xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
            xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math"
            xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
            xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
            xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">
  <w:body>
    {''.join(body_parts)}
    <w:sectPr>
      <w:headerReference w:type="default" r:id="rId2"/>
      <w:footerReference w:type="default" r:id="rId3"/>
      <w:pgSz w:w="12240" w:h="15840"/>
      <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="720" w:footer="720" w:gutter="0"/>
    </w:sectPr>
  </w:body>
</w:document>"""

    # Create ZIP archive (DOCX)
    output_filename = "Alnoor_Islami_User_Guide.docx"
    with zipfile.ZipFile(output_filename, 'w', zipfile.ZIP_DEFLATED) as docx:
        docx.writestr('[Content_Types].xml', content_types)
        docx.writestr('_rels/.rels', rels)
        docx.writestr('word/_rels/document.xml.rels', doc_rels)
        docx.writestr('word/document.xml', doc_xml)
        docx.writestr('word/styles.xml', styles)
        docx.writestr('word/header1.xml', header)
        docx.writestr('word/footer1.xml', footer)
        if logo_bytes:
            docx.writestr('word/media/image1.png', logo_bytes)

    print(f"Successfully generated {output_filename} ({os.path.getsize(output_filename)} bytes)")

if __name__ == "__main__":
    generate_docx()
