"""Generate original LearnPilot launcher artwork; requires Pillow."""
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res"
NAVY = (26, 54, 107, 255)
WHITE = (255, 255, 255, 255)
MINT = (113, 224, 196, 255)


def art(size: int, transparent: bool) -> Image.Image:
    scale = 4
    n = size * scale
    im = Image.new("RGBA", (n, n), (0, 0, 0, 0) if transparent else NAVY)
    d = ImageDraw.Draw(im)
    def p(points): return [(round(x * n), round(y * n)) for x, y in points]
    def box(coords): return tuple(round(v * n) for v in coords)
    # Open pages form the base; the rising mint route is the pilot's heading.
    d.polygon(p([(.24,.39),(.43,.38),(.50,.43),(.50,.69),(.44,.65),(.24,.65)]), fill=WHITE)
    d.polygon(p([(.50,.43),(.57,.38),(.76,.39),(.76,.65),(.56,.65),(.50,.69)]), fill=(232,241,255,255))
    d.line(p([(.50,.43),(.50,.69)]), fill=NAVY if transparent else (78,106,157,255), width=max(2, round(n*.011)))
    d.line(p([(.34,.55),(.46,.49),(.56,.54),(.70,.40)]), fill=MINT, width=max(4, round(n*.039)), joint="curve")
    d.ellipse(box((.665,.365,.735,.435)), fill=MINT)
    return im.resize((size,size), Image.Resampling.LANCZOS)


def main():
    (ROOT / "drawable-nodpi").mkdir(parents=True, exist_ok=True)
    art(432, True).save(ROOT / "drawable-nodpi" / "ic_learnpilot_foreground.png")
    for suffix, px in {"mdpi":48,"hdpi":72,"xhdpi":96,"xxhdpi":144,"xxxhdpi":192}.items():
        folder = ROOT / f"mipmap-{suffix}"
        folder.mkdir(parents=True, exist_ok=True)
        art(px, False).save(folder / "ic_launcher.webp", "WEBP", lossless=True)
        art(px, False).save(folder / "ic_launcher_round.webp", "WEBP", lossless=True)
    art(192, False).save(ROOT.parents[3] / "icon-preview.png")


if __name__ == "__main__": main()
