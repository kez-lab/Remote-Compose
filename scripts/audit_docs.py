#!/usr/bin/env python3
"""Repository-local regression checks for Remote Compose documentation."""

from __future__ import annotations

from collections import Counter
from html.parser import HTMLParser
from pathlib import Path
import re
from urllib.parse import unquote, urlparse


ROOT = Path(__file__).resolve().parents[1]
CODELAB = ROOT / "codelab" / "index.html"
WIKI = ROOT / "reference" / "wiki"


class HtmlAudit(HTMLParser):
    def __init__(self) -> None:
        super().__init__()
        self.ids: list[str] = []
        self.steps: list[str] = []
        self.targets: list[str] = []
        self.links: list[str] = []

    def handle_starttag(self, tag: str, attrs: list[tuple[str, str | None]]) -> None:
        values = dict(attrs)
        if values.get("id"):
            self.ids.append(values["id"] or "")
        if values.get("data-step"):
            self.steps.append(values["data-step"] or "")
        if values.get("data-step-target"):
            self.targets.append(values["data-step-target"] or "")
        for name in ("href", "src"):
            if values.get(name):
                self.links.append(values[name] or "")


def assert_local_link(source: Path, link: str) -> None:
    parsed = urlparse(link)
    if parsed.scheme or link.startswith("#"):
        return
    target = (source.parent / unquote(parsed.path)).resolve()
    assert target.exists(), f"broken local link: {source.relative_to(ROOT)} -> {link}"


def audit_codelab() -> None:
    text = CODELAB.read_text(encoding="utf-8")
    parser = HtmlAudit()
    parser.feed(text)

    assert parser.steps == [str(index) for index in range(10)], parser.steps
    assert parser.targets == [str(index) for index in range(10)], parser.targets
    assert all(count == 1 for count in Counter(parser.ids).values()), "duplicate HTML id"
    for link in parser.links:
        assert_local_link(CODELAB, link)

    core_path, _, advanced = text.partition("다른 제작 방식은 별도 심화 과제입니다")
    assert "RemoteText(" not in core_path, "public Compose frontend leaked into core path"
    assert "RemoteText" in advanced, "advanced frontend handoff is missing"
    assert "createRcBuffer" in core_path
    assert "자동으로" in core_path and "RootLayoutComponent" in core_path
    assert "StateLayout(stateIndex = done)" in core_path
    assert "Box(" in core_path, "StateLayout states must be layout children"
    assert "documentWidth = 390" in core_path and "documentHeight = 720" in core_path


def audit_markdown_links() -> None:
    markdown_files = [ROOT / "README.md", ROOT / "codelab" / "README.md"]
    markdown_files.extend(sorted((ROOT / "reference").rglob("*.md")))
    markdown_files.append(ROOT / "samples" / "remote-state-lab" / "README.md")

    pattern = re.compile(r"!?(?:\[[^]]*\])\(([^)]+)\)")
    for source in markdown_files:
        text = source.read_text(encoding="utf-8")
        for match in pattern.finditer(text):
            link = match.group(1).strip().split(" ", 1)[0]
            assert_local_link(source, link)


def audit_wiki_index() -> None:
    index = (WIKI / "index.md").read_text(encoding="utf-8")
    missing = [
        path.name for path in WIKI.glob("*.md") if path.name != "index.md" and path.name not in index
    ]
    assert not missing, f"wiki pages missing from index: {missing}"


def main() -> None:
    audit_codelab()
    audit_markdown_links()
    audit_wiki_index()
    print("documentation audit passed")


if __name__ == "__main__":
    main()
