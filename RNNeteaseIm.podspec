# coding: utf-8


require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))
version = package['version']

source = { :git => 'https://github.com/96VuHoangDuy/react-native-netease-im.git' }
if version == '1000.0.0'
  # This is an unpublished version, use the latest commit hash of the react-native repo, which we’re presumably in.
  source[:commit] = `git rev-parse HEAD`.strip
else
  source[:tag] = "v#{version}"
end

Pod::Spec.new do |s|
  s.name                  = "RNNeteaseIm"
  s.version               = version
  s.summary            = "A React component for netease-im."
  s.homepage          = "https://github.com/96VuHoangDuy/react-native-netease-im"
  s.requires_arc       = true
  s.license                = "None"
  s.author                 = {"DavidVu" => "duy.vu@just.engineer"}
  s.platform              = :ios , "12.0"
  s.source                 = source
  s.source_files        = "**/*.{h,m}"

  s.dependency 'React-Core'
  # NIMSDK_LITE (không phải NIMSDK full) — vì NERtcCallKit bắt buộc NIMSDK_LITE; full NIMSDK
  # trùng framework (nimsdk/nimsocketrocket/nimquic.xcframework) với LITE → pod install fail.
  # LITE vẫn cung cấp module NIMSDK (#import <NIMSDK/NIMSDK.h> vẫn chạy). MẤT NIMAVChat (đã gỡ code legacy).
  # /FTS: full-text-search local (tương đương lucene bên Android).
  s.dependency "NIMSDK_LITE", "10.9.53"
  s.dependency "NIMSDK_LITE/FTS", "10.9.53"
  s.dependency "Reachability"

  # NERTC Call Kit (voice call, Phase 3) — call-ui 4.1.0 ↔ NIM 10.9.53 ↔ NERTC 5.9.10.
  # Dùng subspec NOS_Special (khớp NIMSDK default subspec = NOS ở trên; KHÔNG kéo IM SDK riêng).
  # ⚠️ VERIFY khi `pod install`: nếu lỗi trùng/thiếu IM SDK (NIMSDK vs NIMSDK_LITE), xem
  #    docs/call-ios-native/01-integration-ios.md §2 (Option B) — có thể phải điều chỉnh subspec/LITE.
  #    Giữ "NIMSDK" (full) vì code legacy NIMAVChat (ImConfig.h/NTESBundleSetting) cần nó.
  s.dependency "NERtcCallKit/NOS_Special"
  s.dependency "NERtcCallUIKit/NOS_Special"
  s.dependency "NERtcSDK/RtcBasic", "5.9.10"

end
