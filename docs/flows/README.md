# Flows — luồng chạy xuyên tầng

Mỗi file ở đây mô tả **một luồng của tính năng**, kể bằng code: điểm vào → đi qua những chặng nào
(kèm `file:line`) → hỏng ở đâu. Khác với `../playbook/REPO_MAP.md` / `../common/ARCHITECTURE.md`
vốn mô tả *cấu trúc*.

**Luật viết + format đầy đủ: `RULE-FLOW-01` trong `CLAUDE.md` ở root repo.**
Không chép lại ở đây để tránh hai bản lệch nhau.

Ba điều dễ sai nhất:
1. Mọi chặng phải có `file:line` — không verify được trong 5 giây thì viết sai cách.
2. Không viết flow đầu cơ. Chỉ lưu luồng vừa thật sự trace và đã kiểm chứng.
3. Chưa chắc thì ghi `⚠️ chưa verify`, đừng viết giọng tự tin.

Thư mục này **không** được `@import` — đọc theo nhu cầu.

---

_Chưa có flow nào. File đầu tiên sẽ được nhặt ra từ task thật kế tiếp._
