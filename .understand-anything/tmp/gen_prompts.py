#!/usr/bin/env python3
import json
import os

PROJECT_ROOT = "/mnt/data1/KMA/on-thi-bang-lai-xe"
SKILL_DIR = "/home/trtung/.claude/plugins/cache/understand-anything/understand-anything/2.7.6/skills/understand"
TOTAL_BATCHES = 19

def format_batch_prompt(b):
    files = b.get('files', [])
    idx = b['batchIndex']
    imp = b.get('batchImportData', {})
    nm = b.get('neighborMap', {})

    file_lines = []
    for i, f in enumerate(files):
        file_lines.append(f'{i+1}. `{f["path"]}` ({f.get("sizeLines",0)} lines, language: `{f.get("language","unknown")}`, fileCategory: `{f.get("fileCategory","code")}`)')

    return f"""Analyze these files and produce GraphNode and GraphEdge objects.
Project root: {PROJECT_ROOT}
Project: on-thi-bang-lai-xe (Vietnamese Driving License Exam App)
Languages: Java, XML, Gradle, JSON
Batch: {idx}/{TOTAL_BATCHES}
Skill directory (for bundled scripts): {SKILL_DIR}
Output: write to {PROJECT_ROOT}/.understand-anything/intermediate/batch-{idx}.json

Pre-resolved import data for this batch (use directly — do NOT re-resolve imports from source):
```json
{json.dumps(imp, indent=2)}
```

Cross-batch neighbors with their exported symbols (confidence boost for cross-batch edges):
```json
{json.dumps(nm, indent=2)}
```

Files to analyze in this batch (every entry MUST be passed through to batchFiles with all four fields — path, language, sizeLines, fileCategory):
{chr(10).join(file_lines)}

**Additional context from main session:**

Project: on-thi-bang-lai-xe — Vietnamese Driving License Exam Practice App (Android/Java)
Languages: Java, XML, Gradle, JSON, Python

> **Language directive**: Generate all textual content (summaries, descriptions, tags, titles, languageNotes, languageLesson) in **English**. Maintain technical accuracy while using natural, native-level phrasing in the target language. Keep technical terms in English when no standard translation exists (e.g., "middleware", "hook", "barrel")."""

# Generate prompts for all batches
for batch_num in range(1, TOTAL_BATCHES + 1):
    info_path = f"{PROJECT_ROOT}/.understand-anything/tmp/batch_{batch_num}_info.json"
    if os.path.exists(info_path):
        with open(info_path) as f:
            b = json.load(f)
        prompt = format_batch_prompt(b)
        out_path = f"{PROJECT_ROOT}/.understand-anything/tmp/batch_{batch_num}_prompt.txt"
        with open(out_path, 'w') as f:
            f.write(prompt)
        print(f"Written prompt for batch {batch_num}")

print("All prompts written!")
