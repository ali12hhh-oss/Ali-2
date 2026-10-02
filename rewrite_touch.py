import re

with open('app/src/main/java/com/tharunbirla/librecuts/customviews/TrackTrimView.kt', 'r') as f:
    content = f.read()

# Remove the early return for isTrimEnabled
content = re.sub(r'        if \(!isTrimEnabled\) \{\n\s*// When clip is not selected for trimming, allow finger swipe to scroll the timeline\n\s*return false\n\s*\}\n', '', content)

# Change ACTION_DOWN
down_regex = r'dragTarget = when \{\n.*?\n.*?\n.*?\n.*?\n\s*\}'
new_down = '''dragTarget = if (isTrimEnabled) {
                    when {
                        event.x in (startX - hitMargin)..leftZoneEnd -> DragTarget.LEFT
                        event.x in rightZoneStart..(endX + hitMargin) -> DragTarget.RIGHT
                        event.x > leftZoneEnd && event.x < rightZoneStart -> DragTarget.CENTER
                        else -> DragTarget.NONE
                    }
                } else {
                    // If not enabled for trimming, any touch is a center tap (to select it)
                    DragTarget.CENTER
                }'''
content = re.sub(down_regex, new_down, content, flags=re.DOTALL)

with open('app/src/main/java/com/tharunbirla/librecuts/customviews/TrackTrimView.kt', 'w') as f:
    f.write(content)
