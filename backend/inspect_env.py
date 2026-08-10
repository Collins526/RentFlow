from pathlib import Path
p = Path('backend/.env')
text = p.read_text()
for i, line in enumerate(text.splitlines(), 1):
    print(f"{i}: {repr(line)}")
