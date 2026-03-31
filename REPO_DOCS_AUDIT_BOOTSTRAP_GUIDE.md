# Repository Docs Audit & Bootstrap Guide (Portable)

Mục tiêu: dùng 1 file này để audit bất kỳ repo nào và tạo bộ tài liệu `.md` cần thiết cho:
- AI agents (Codex/Claude/khác)
- đội phát triển (architecture, decisions, contributing, feature docs)

File này độc lập với project hiện tại, có thể copy nguyên sang repo khác.

## 1) Khi nào dùng
Dùng guide này khi repo:
- chưa có docs rõ ràng,
- docs bị phân mảnh,
- hoặc cần chuẩn hóa để AI làm việc đúng kiến trúc.

## 2) Kết quả cần tạo
Sau audit, repo nên có tối thiểu:

1. `README.md`
2. `AI_PLAYBOOK.md`
3. `AGENTS.md` (ưu tiên Codex)
4. `CLAUDE.md` (ưu tiên Claude)
5. `docs/README.md`
6. `docs/common/ARCHITECTURE.md`
7. `docs/common/DECISIONS.md`
8. `docs/common/CONTRIBUTING.md`
9. `docs/features/<feature>/README.md` cho từng feature hiện có
10. `docs/features/_TEMPLATE.md`

Khuyến nghị thêm (khi cần):
- `docs/playbook/STACK_AND_TOOLS.md`
- `docs/playbook/REPO_MAP.md`
- `docs/playbook/COMMANDS.md`
- `docs/playbook/ARCHITECTURE_RULES.md`
- `docs/playbook/DESIGN_SYSTEM_THEME.md` (nếu có design tokens / dark-light)
- `docs/playbook/GAPS.md`
- `docs/playbook/CURRENT_CONTEXT.md`

## 3) Luật tạo nội dung
- Chỉ ghi những gì xác thực được từ code/config.
- Thông tin không có trong repo: để trống.
- Không suy đoán công nghệ/luồng nghiệp vụ nếu chưa thấy bằng chứng.
- Nội dung dài: tách file riêng rồi link từ file điều hướng.
- Không để 1 section trong `AI_PLAYBOOK.md` quá dài; giữ vai trò “mục lục điều phối”.

## 4) Quy trình audit chuẩn (4 pha)

## Pha A - Discovery (thu thập)
Thu thập các điểm neo:
- runtime/framework: `package.json`, lockfile
- build/release: `eas.json`, CI config, scripts
- architecture: cấu trúc `src/`, `apps/`, `services/`
- navigation/routes (frontend app)
- state management
- API layer và error handling
- local storage/database
- theme/tokens/dark-light
- testing/lint/typecheck

Gợi ý command (portable):
```bash
ls -la
find . -maxdepth 3 -type f | head -200
rg --files
rg -n "redux|mobx|zustand|recoil|xstate"
rg -n "axios|fetch|graphql|apollo|react-query|tanstack"
rg -n "jest|vitest|detox|cypress|playwright"
rg -n "theme|tokens|dark|light|colorScheme"
```

## Pha B - Synthesis (chuẩn hóa hiểu biết)
Chuẩn hóa thành 6 nhóm:
1. Product snapshot
2. Tech stack chính xác
3. Repo map + module boundaries
4. Run/build/verify commands
5. Architecture conventions
6. Gaps (chưa đủ dữ liệu)

## Pha C - Generate (tạo docs)
Tạo docs theo thứ tự:
1. `docs/common/*`
2. `docs/features/*`
3. `docs/playbook/*` (nếu cần)
4. `AI_PLAYBOOK.md`
5. `AGENTS.md` + `CLAUDE.md`
6. update `docs/README.md` + `README.md`

## Pha D - Validate (xác minh)
Checklist:
- file paths trong docs đều tồn tại
- command trong docs chạy được (hoặc ghi rõ chưa kiểm chứng)
- không còn mâu thuẫn kiến trúc giữa các file
- mọi feature chính đều có `docs/features/<feature>/README.md`

## 5) Mẫu cấu trúc docs khuyến nghị
```text
.
├── README.md
├── AI_PLAYBOOK.md
├── AGENTS.md
├── CLAUDE.md
└── docs/
    ├── README.md
    ├── common/
    │   ├── ARCHITECTURE.md
    │   ├── DECISIONS.md
    │   └── CONTRIBUTING.md
    ├── features/
    │   ├── _TEMPLATE.md
    │   ├── <feature-a>/README.md
    │   └── <feature-b>/README.md
    └── playbook/
        ├── STACK_AND_TOOLS.md
        ├── REPO_MAP.md
        ├── COMMANDS.md
        ├── ARCHITECTURE_RULES.md
        ├── DESIGN_SYSTEM_THEME.md
        ├── GAPS.md
        └── CURRENT_CONTEXT.md
```

## 6) Tiêu chí viết từng file

## `AI_PLAYBOOK.md`
Phải có:
- mission + priorities
- project snapshot
- tech stack
- repo map
- run/build/verify
- workflow chuẩn Plan -> Execute -> Verify
- architecture conventions
- output contract khi trả lời
- current context (editable)

Không nên nhồi mô tả dài; link sang `docs/playbook/*`.

## `AGENTS.md`
Tập trung vào hành vi agent khi thao tác code:
- read-first list
- guardrails kiến trúc
- safety rules
- verify trước handoff
- response contract
- rule confirm cập nhật docs feature sau khi xong task

## `CLAUDE.md`
Tương tự `AGENTS.md`, giữ wording phù hợp Claude, cùng guardrails và docs confirmation rule.

## `docs/common/ARCHITECTURE.md`
- stack tổng quan
- data flow chuẩn
- module boundaries
- non-goals kiến trúc

## `docs/common/DECISIONS.md`
- ADR lite theo ID
- mỗi quyết định gồm: Status / Why / Tradeoff

## `docs/common/CONTRIBUTING.md`
- coding conventions
- branching/PR rules
- quality gates tối thiểu
- docs update policy

## `docs/features/<feature>/README.md`
Tối thiểu gồm:
- Scope
- Entry points
- State & data flow
- Business rules
- Error handling notes

## 7) Cách xác định feature từ repo
Ưu tiên theo thứ tự:
1. thư mục màn hình/route (`screens`, `pages`, `routes`)
2. store/service/domain folders
3. menu/tab navigation
4. API domains

Mỗi feature nên ánh xạ được:
- UI entrypoint
- state holder
- API/data source

## 8) Rule dọn docs cũ
- Giữ lại docs còn giá trị dài hạn (architecture/rules/setup).
- Gộp docs “fix summary” rời rạc thành feature docs hoặc archive.
- Nếu xóa nhiều file, tạo 1 commit riêng để dễ review.

## 9) Rule xác nhận với owner (bắt buộc)
Sau khi hoàn tất task code:
- Nếu sửa feature hiện có: confirm owner có muốn update `docs/features/<feature>/README.md` không.
- Nếu thêm feature mới: confirm owner có muốn tạo `docs/features/<new-feature>/README.md` không.

## 10) Prompt mẫu để chạy trên repo khác
Copy đoạn sau cho agent:

```text
Hãy audit repo này và tạo bộ docs chuẩn gồm:
- AI_PLAYBOOK.md, AGENTS.md, CLAUDE.md
- docs/common/{ARCHITECTURE,DECISIONS,CONTRIBUTING}.md
- docs/features/<feature>/README.md cho toàn bộ feature hiện có
- docs/README.md và README.md cập nhật map tài liệu

Yêu cầu:
1) Chỉ dùng thông tin xác thực từ code/config.
2) Nếu thông tin không có thì để trống.
3) Section dài thì tách file dưới docs/playbook và link từ AI_PLAYBOOK.md.
4) Không đổi kiến trúc code, chỉ tạo/cập nhật docs.
5) Sau khi hoàn tất, liệt kê files changed + gaps cần con người điền.
```

## 11) Definition of Done
Hoàn tất khi:
- bộ file mục (2) đã có,
- docs map đọc được từ top-down,
- không mâu thuẫn kiến trúc giữa các file,
- có danh sách gaps rõ ràng để team điền thêm.
