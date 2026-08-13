#!/usr/bin/env python3
"""Genera fdroid/repo/index.xml (formato v0/v1) para un repositorio F-Droid auto-hospedado."""
import argparse
import base64
import hashlib
import os
import re
import subprocess
import time
from xml.sax.saxutils import escape

KEYTOOL = r"C:\Program Files\Java\jdk-24\bin\keytool.exe"
JAR = r"C:\Program Files\Java\jdk-24\bin\jar.exe"
JARSIGNER = r"C:\Program Files\Java\jdk-24\bin\jarsigner.exe"


def load_props(path):
    props = {}
    with open(path, encoding="ascii") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            k, v = line.split("=", 1)
            props[k] = v
    return props


def cert_base64(keystore, alias, storepass):
    out = subprocess.run(
        [KEYTOOL, "-exportcert", "-alias", alias, "-keystore", keystore,
         "-storepass", storepass],
        check=True, capture_output=True,
    ).stdout
    return base64.b64encode(out).decode("ascii")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--apk", required=True)
    parser.add_argument("--out", required=True)
    parser.add_argument("--repo-url", required=True)
    parser.add_argument("--repo-name", default="AgroAtlautla")
    parser.add_argument("--repo-desc", default="Repositorio F-Droid de AgroAtlautla")
    parser.add_argument("--web", default="")
    parser.add_argument("--icon", default="")
    parser.add_argument("--min-sdk", default=24)
    parser.add_argument("--target-sdk", default=35)
    args = parser.parse_args()

    props = load_props("keystore.properties")

    apk_bytes = open(args.apk, "rb").read()
    sha256 = hashlib.sha256(apk_bytes).hexdigest()
    size = len(apk_bytes)

    sig = cert_base64(
        os.path.join("keystore", props.get("keystoreFile", "agroatlautla-release.jks")),
        props["keyAlias"], props["storePassword"],
    )
    pubkey = cert_base64(
        os.path.join("keystore", "fdroid-repo.jks"),
        props["repoAlias"], props["repoPassword"],
    )

    apkname = os.path.basename(args.apk)

    with open("app/build.gradle.kts", encoding="utf-8") as f:
        gradle_src = f.read()
    version_code = re.search(r"versionCode\s*=\s*(\d+)", gradle_src).group(1)
    version_name = re.search(r'versionName\s*=\s*"([^"]+)"', gradle_src).group(1)

    added = time.strftime("%Y-%m-%d")
    timestamp = str(int(time.time()))

    icon_attr = f' icon="{escape(args.icon)}"' if args.icon else ""
    web_attr = f' web="{escape(args.web)}"' if args.web else ""

    xml = f"""<?xml version="1.0" encoding="utf-8"?>
<fdroid>
  <repo icon="{escape(args.icon)}" name="{escape(args.repo_name)}" pubkey="{pubkey}" url="{escape(args.repo_url)}" version="1" timestamp="{timestamp}">
    <description>{escape(args.repo_desc)}</description>
  </repo>
  <application id="com.agroatlautla.app">
    <id>com.agroatlautla.app</id>
    <added>{added}</added>
    <lastupdated>{added}</lastupdated>
    <name>AgroAtlautla</name>
    <summary>Apoyo digital para productores del campo en Atlautla</summary>
    <license>GPL-3.0-only</license>
    <categories>agriculture,productivity</categories>{web_attr}
    <package versioncode="{version_code}" versionname="{version_name}" apkname="{apkname}" hash="sha256:{sha256}" size="{size}" sig="{sig}" added="{added}" minSdkVersion="{args.min_sdk}" targetSdkVersion="{args.target_sdk}" />
  </application>
</fdroid>
"""
    os.makedirs(args.out, exist_ok=True)
    with open(os.path.join(args.out, "index.xml"), "w", encoding="utf-8") as f:
        f.write(xml)
    print(f"index.xml escrito en {args.out}")
    print(f"sha256: {sha256}")
    print(f"size: {size}")


if __name__ == "__main__":
    main()