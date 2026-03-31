# Commands

## Command khuyến nghị cho repo này

### TypeScript check một phần

```bash
npx tsc --noEmit
```

Ghi chú:

- command này chỉ cover `src/**/*` theo `tsconfig.json`
- không cover `index.ts` và `Utils.ts`
- workspace audit hiện tại không có binary `tsc` local sẵn để xác minh command này end-to-end

### Audit source nhanh

```bash
rg --files src android ios
rg -n "RCT_EXPORT_METHOD|@ReactMethod" ios/RNNeteaseIm/RNNeteaseIm android/src/main/java/com/netease/im
rg -n "observe[A-Za-z]+" src android/src/main/java/com/netease/im ios/RNNeteaseIm/RNNeteaseIm
```

## Command hữu ích nhưng không thể xác minh end-to-end chỉ với repo này

### Install vào consumer app

```bash
npm install react-native-netease-im
npx pod-install
```

### iOS / Android consumer app build

Phải chạy ở app host, không chạy trực tiếp trong repo thư viện này.

## Command hiện không hữu dụng

### `npm test`

`package.json` hiện định nghĩa:

```bash
echo "Error: no test specified" && exit 1
```

Nghĩa là chưa có test suite thực tế trong repo.
