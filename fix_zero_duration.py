import re

with open('app/src/main/java/com/tharunbirla/librecuts/VideoEditingActivity.kt', 'r') as f:
    content = f.read()

# Fix Text
content = content.replace(
    'val end = minOf(start + 3000L, getTotalSequenceDuration())',
    'val seqDur = getTotalSequenceDuration()\n                        val end = if (seqDur == 0L) start + 3000L else minOf(start + 3000L, seqDur)'
)

# Fix PIP
content = content.replace(
    'val end = minOf(start + duration, getTotalSequenceDuration())',
    'val seqDur2 = getTotalSequenceDuration()\n                            val end = if (seqDur2 == 0L) start + duration else minOf(start + duration, seqDur2)'
)

with open('app/src/main/java/com/tharunbirla/librecuts/VideoEditingActivity.kt', 'w') as f:
    f.write(content)
