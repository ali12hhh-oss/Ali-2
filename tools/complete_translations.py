#!/usr/bin/env python3
"""Complete missing Android string translations for every locale used by Ali-2.

Existing translations are kept. Missing keys are translated from the English
resource using Google Translate via deep-translator. Android format placeholders
are protected during translation so values such as %1$s and %1$d remain valid.
"""
from pathlib import Path
import html
import re
import sys
import time

from deep_translator import GoogleTranslator

ROOT = Path("app/src/main/res")
SOURCE = ROOT / "values" / "strings.xml"

TARGETS = {
    "values-ar": "ar",
    "values-de": "de",
    "values-et": "et",
    "values-hi": "hi",
    "values-pt-rBR": "pt",
    "values-sk": "sk",
    "values-ta": "ta",
    "values-zh-rCN": "zh-CN",
    "values-ja": "ja",
    "values-ko": "ko",
}

# These are product/technical identifiers, not prose to translate.
KEEP_AS_IS = {
    "app_name", "str_downloads_librecuts", "str_subtitles_srt",
    "str_v1_0_beta4", "str_version_1_0", "str_github_sponsors",
    "str_discord_server", "font_preview", "default_font",
    "error_code", "app_version_label", "device_label",
    "android_version_label", "storage_root_path",
}

STRING_RE = re.compile(
    r'<string(?P<attrs>[^>]*?)\sname="(?P<key>[^"]+)"(?P<attrs2>[^>]*)>(?P<value>.*?)</string>',
    re.S,
)

def read_strings(path):
    text = path.read_text(encoding="utf-8")
    return {m.group("key"): m.group("value") for m in STRING_RE.finditer(text)}

def protect(value):
    placeholders = []
    def repl(m):
        token = f"__VIDORA_PH_{len(placeholders)}__"
        placeholders.append(m.group(0))
        return token
    value = re.sub(r"%\d+\$[sdif]", repl, value)
    return value, placeholders

def restore(value, placeholders):
    for i, original in enumerate(placeholders):
        value = value.replace(f"__VIDORA_PH_{i}__", original)
    return value

def clean_for_translation(value):
    value = value.replace("\\'", "'")
    value = value.replace("\n", " ")
    return value.strip()

def xml_escape(value):
    # Android resource values are sensitive to escape handling.
    # Remove stray backslashes and encode apostrophes as XML entities.
    value = re.sub(r"\\(?![nrt\\\"'u])", "", value)
    return html.escape(value, quote=False).replace("'", "&apos;")

def translate_missing(missing, target):
    if not missing:
        return {}
    translator = GoogleTranslator(source="en", target=target)
    result = {}
    # translate_batch is implemented as a sequence of requests by the library;
    # small batches keep failures recoverable and avoid oversized requests.
    items = list(missing.items())
    for start in range(0, len(items), 20):
        batch = items[start:start + 20]
        texts = []
        metadata = []
        for key, raw in batch:
            protected, placeholders = protect(clean_for_translation(raw))
            if not protected or protected.isdigit():
                result[key] = raw
                continue
            texts.append(protected)
            metadata.append((key, placeholders, raw))
        if texts:
            translated = None
            for attempt in range(4):
                try:
                    translated = translator.translate_batch(texts)
                    break
                except Exception as exc:
                    if attempt == 3:
                        print(f"Translation batch failed for {target}: {exc}", file=sys.stderr)
                    else:
                        time.sleep(2 ** attempt)
            if translated is None:
                translated = texts
            for (key, placeholders, raw), value in zip(metadata, translated):
                if not value:
                    value = raw
                result[key] = restore(value, placeholders)
        print(f"{target}: {min(start + 20, len(items))}/{len(items)}", flush=True)
        time.sleep(0.2)
    return result

def append_missing(path, additions):
    if not additions:
        return False
    text = path.read_text(encoding="utf-8")
    lines = []
    for key, value in additions.items():
        # Preserve Android escaped newlines and XML entities.
        value = value.replace("\\'", "'")
        lines.append(f'    <string name="{key}">{xml_escape(value)}</string>')
    insertion = "\n" + "\n".join(lines) + "\n"
    marker = "</resources>"
    if marker not in text:
        raise RuntimeError(f"Invalid resources file: {path}")
    path.write_text(text.replace(marker, insertion + marker, 1), encoding="utf-8")
    return True

source = read_strings(SOURCE)
changed = []

for dirname, target in TARGETS.items():
    path = ROOT / dirname / "strings.xml"
    if not path.exists():
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n</resources>\n', encoding="utf-8")
    existing = read_strings(path)
    missing = {k: v for k, v in source.items() if k not in existing}
    missing = {k: v for k, v in missing.items() if k not in KEEP_AS_IS}
    additions = translate_missing(missing, target)
    # Add protected identifiers without a translation request.
    for key, value in source.items():
        if key not in existing and key in KEEP_AS_IS:
            additions[key] = value
    if append_missing(path, additions):
        changed.append((dirname, len(additions)))

# Verify every locale has every source key.
failed = False
for dirname in TARGETS:
    keys = read_strings(ROOT / dirname / "strings.xml")
    missing = [k for k in source if k not in keys]
    if missing:
        failed = True
        print(f"ERROR {dirname}: {len(missing)} keys still missing", file=sys.stderr)
    else:
        print(f"OK {dirname}: {len(keys)}/{len(source)} keys")

print("Changed:", changed)
if failed:
    sys.exit(1)
