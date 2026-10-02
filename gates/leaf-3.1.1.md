# Gates: GPU Color Grade & Blend Shader Pipeline (Leaf 3.1.1)

Scope: Implement OpenGL ES GLSL shaders for real-time 12-channel color adjustments and PIP blend modes

- [x] G1: ShaderPrograms.kt exists with vertex, color grade fragment, and blend mode GLSL shaders
  CHECK: node -e 'const fs=require("fs"); const s=fs.readFileSync("app/src/main/java/com/tharunbirla/librecuts/render/gpu/ShaderPrograms.kt","utf8"); process.exit(s.includes("COLOR_GRADE_FRAGMENT_SHADER") && s.includes("BLEND_MODE_FRAGMENT_SHADER") ? 0 : 1);'
  EXPECT: /.*/
  EVIDENCE: (no output)

- [x] G2: GPUColorGradeFilter.kt exists with applyColorGrade and uniform binders
  CHECK: node -e 'const fs=require("fs"); const s=fs.readFileSync("app/src/main/java/com/tharunbirla/librecuts/render/gpu/GPUColorGradeFilter.kt","utf8"); process.exit(s.includes("class GPUColorGradeFilter") && s.includes("fun applyColorGrade") ? 0 : 1);'
  EXPECT: /.*/
  EVIDENCE: (no output)
