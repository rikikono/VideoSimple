# Nội dung bổ sung cho báo cáo [`BaoCaoFinal.docx`](BaoCaoFinal.docx)

## 1. Nhận xét nhanh về báo cáo hiện tại

Sau khi rà soát nội dung trong [`BaoCaoFinal.docx`](BaoCaoFinal.docx), báo cáo hiện tại đã có phần mở đầu phù hợp với bài tập lớn xây dựng ứng dụng xem video trên Android, nhưng từ phần **mục lục** và nhiều nội dung trong **Chương I, Chương II, Chương III, Chương IV** vẫn còn bị lẫn nội dung của một bài cũ về **PHP, HTML, CSS, MySQL, website bán hàng quần áo**. Vì vậy, nếu nộp nguyên file hiện tại thì nội dung sẽ bị lệch với bài tập lớn hiện tại khá rõ.

Các điểm chưa phù hợp chính:

- Mục lục vẫn ghi các mục như PHP, HTML, CSS, MySQL, website bán hàng.
- Chương I đang giải thích nhiều công nghệ web không liên quan trực tiếp đến bài tập lớn ứng dụng Android hiện tại.
- Chương II mô tả hệ thống website bán hàng thay vì ứng dụng xem video.
- Chương III kiểm thử theo hướng website thương mại điện tử, chưa bám sát ứng dụng phát video.
- Chương IV kết luận cũng đang nói về website bán hàng.

Tài liệu này cung cấp **nội dung thay thế/bổ sung hoàn chỉnh** để bạn dán lại vào file Word, giúp báo cáo khớp với dự án Android trong thư mục [`app`](app).

---

## 2. Gợi ý chỉnh lại mục lục

Bạn nên chỉnh phần mục lục theo cấu trúc sau:

- LỜI NÓI ĐẦU
- CHƯƠNG I: GIỚI THIỆU TỔNG QUAN VỀ BÀI TẬP LỚN
  - 1.1. Bối cảnh thực tiễn của bài tập lớn
  - 1.2. Lý do lựa chọn bài tập lớn
  - 1.3. Mục tiêu của bài tập lớn
  - 1.4. Phạm vi thực hiện
  - 1.5. Công nghệ sử dụng
  - 1.6. Kiến trúc và tổ chức ứng dụng
- CHƯƠNG II: PHÂN TÍCH VÀ XÂY DỰNG ỨNG DỤNG
  - 2.1. Khảo sát yêu cầu hệ thống
  - 2.2. Phân tích chức năng
  - 2.3. Thiết kế giao diện
  - 2.4. Thiết kế dữ liệu cục bộ
  - 2.5. Cài đặt các chức năng chính
- CHƯƠNG III: KIỂM THỬ VÀ ĐÁNH GIÁ
  - 3.1. Mục tiêu kiểm thử
  - 3.2. Các kịch bản kiểm thử
  - 3.3. Kết quả kiểm thử
  - 3.4. Đánh giá ưu điểm và hạn chế
- CHƯƠNG IV: KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN
  - 4.1. Kết luận
  - 4.2. Hạn chế
  - 4.3. Hướng phát triển
- BẢNG PHÂN CÔNG NHIỆM VỤ

---

## 3. Nội dung thay thế cho CHƯƠNG I

## CHƯƠNG I: GIỚI THIỆU TỔNG QUAN VỀ BÀI TẬP LỚN

### 1.1. Bối cảnh thực tiễn của bài tập lớn

Trong những năm gần đây, thiết bị di động thông minh, đặc biệt là điện thoại Android, đã trở thành phương tiện phổ biến phục vụ học tập, làm việc và giải trí. Cùng với sự phát triển của hạ tầng mạng và bộ nhớ thiết bị, nhu cầu xem video trên điện thoại ngày càng tăng mạnh. Người dùng không chỉ cần phát các video lưu trữ cục bộ mà còn mong muốn có thể quản lý danh sách phát, đánh dấu vị trí đang xem, ghi chú nội dung quan trọng và phát video trực tuyến bằng đường dẫn từ Internet.

Trên thực tế, nhiều ứng dụng phát video hiện nay có giao diện phức tạp, chứa quảng cáo, hoặc không hỗ trợ đầy đủ các tính năng cá nhân hóa như lưu lịch sử xem, tạo playlist riêng, thêm ghi chú tại từng mốc thời gian hay lưu bookmark trong video. Vì vậy, việc xây dựng một ứng dụng xem video trên Android với giao diện trực quan, dễ sử dụng và tích hợp các chức năng cần thiết là một hướng thực hiện phù hợp với môn học Lập trình ứng dụng di động.

Bài tập lớn này tập trung xây dựng một ứng dụng Android có khả năng quét video trong thiết bị, hiển thị danh sách video, phát video bằng trình phát tích hợp, hỗ trợ thao tác tua nhanh/tua lùi, khóa màn hình điều khiển, điều chỉnh âm lượng/độ sáng, quản lý playlist, lưu lịch sử xem, phát video online và xem thư viện ảnh. Đây là một bài toán có tính ứng dụng cao, gắn với nhu cầu sử dụng thực tế của người dùng di động hiện nay.

### 1.2. Lý do lựa chọn bài tập lớn

Bài tập lớn “Xây dựng ứng dụng xem video trên Android” được lựa chọn vì các lý do sau:

Thứ nhất, phát video là một nhu cầu rất phổ biến trên điện thoại thông minh. Hầu hết người dùng đều có nhu cầu xem lại video cá nhân, video học tập, video giải trí hoặc các nội dung trực tuyến.

Thứ hai, bài tập lớn cho phép vận dụng tổng hợp nhiều kiến thức quan trọng trong phát triển ứng dụng Android như Activity, ListView, Adapter, MediaPlayer, SQLite, quyền truy cập bộ nhớ, xử lý giao diện XML, quản lý dữ liệu cục bộ và tương tác với tài nguyên hệ thống.

Thứ ba, ứng dụng xem video là một bài toán vừa sức nhưng vẫn đủ độ sâu để phát triển thành một sản phẩm có nhiều chức năng thực tế. Không chỉ dừng ở khả năng phát video cơ bản, ứng dụng còn có thể mở rộng thành công cụ quản lý nội dung cá nhân hóa cho người dùng.

Thứ tư, bài tập lớn phù hợp với định hướng học tập và thực hành của môn Lập trình ứng dụng di động, giúp sinh viên tiếp cận đầy đủ quy trình từ khảo sát nhu cầu, thiết kế giao diện, xây dựng chức năng đến kiểm thử và đánh giá sản phẩm.

### 1.3. Mục tiêu của bài tập lớn

Mục tiêu tổng quát của bài tập lớn là xây dựng một ứng dụng Android hỗ trợ xem video thuận tiện, thân thiện và đáp ứng được các thao tác thường gặp của người dùng.

Các mục tiêu cụ thể gồm:

- Quét và hiển thị danh sách video có trong bộ nhớ thiết bị.
- Hỗ trợ sắp xếp video theo tên, ngày hoặc dung lượng.
- Phát video bằng trình phát tích hợp với các thao tác play/pause, next/previous, tua nhanh/tua lùi.
- Hỗ trợ chế độ toàn màn hình, khóa điều khiển, điều chỉnh âm lượng và độ sáng bằng thao tác cảm ứng.
- Lưu lịch sử xem để người dùng có thể tiếp tục xem từ vị trí trước đó.
- Hỗ trợ đánh dấu video yêu thích.
- Tạo và quản lý playlist cá nhân.
- Thêm bookmark và ghi chú tại các mốc thời gian trong video.
- Lưu danh sách video online bằng URL và phát trực tiếp khi có kết nối mạng.
- Bổ sung thư viện xem ảnh để mở rộng tính đa phương tiện của ứng dụng.

### 1.4. Phạm vi thực hiện

Bài tập lớn tập trung xây dựng ứng dụng trên nền tảng Android bằng ngôn ngữ Java trong môi trường Android Studio. Dữ liệu của ứng dụng chủ yếu được quản lý cục bộ bằng SQLite và dữ liệu video trên thiết bị được truy xuất thông qua MediaStore cùng các tiện ích hỗ trợ.

Phạm vi của bài tập lớn bao gồm:

- Phát video cục bộ trên thiết bị Android.
- Quản lý dữ liệu người dùng ở mức cục bộ như lịch sử xem, yêu thích, playlist, bookmark, note và danh sách video online.
- Thiết kế giao diện bằng XML theo hướng đơn giản, dễ thao tác.
- Kiểm thử chức năng trên thiết bị hoặc máy ảo Android.

Bài tập lớn chưa tập trung vào:

- Đồng bộ dữ liệu lên máy chủ từ xa.
- Đăng nhập tài khoản trực tuyến.
- Gợi ý nội dung bằng trí tuệ nhân tạo.
- Hỗ trợ tất cả giao thức truyền phát nâng cao như DRM, livestream chuyên dụng hoặc các nền tảng video có cơ chế bảo vệ nội dung riêng.

### 1.5. Công nghệ sử dụng

Ứng dụng được xây dựng dựa trên các công nghệ chính sau:

**Ngôn ngữ Java:** Đây là ngôn ngữ lập trình chính của ứng dụng. Java được sử dụng để cài đặt luồng xử lý nghiệp vụ, tương tác với giao diện, điều khiển trình phát video và quản lý dữ liệu cục bộ.

**XML:** Dùng để thiết kế giao diện người dùng cho các màn hình của ứng dụng như màn hình danh sách video, màn hình phát video, màn hình playlist, danh sách video online và thư viện ảnh.

**Android SDK:** Cung cấp các thành phần nền tảng để xây dựng ứng dụng như Activity, Intent, ListView, Adapter, MediaPlayer, SurfaceView, AlertDialog, Permission API và các thành phần hệ thống khác.

**SQLite:** Được sử dụng để lưu trữ dữ liệu cục bộ như lịch sử xem, playlist, danh sách video trong playlist, video yêu thích, bookmark, note và video online.

**MediaPlayer và SurfaceView:** Hỗ trợ phát nội dung video bên trong ứng dụng, đồng thời cho phép điều khiển các thao tác phát/tạm dừng/tua video và hiển thị hình ảnh video lên màn hình.

**MediaStore:** Hỗ trợ quét và lấy danh sách video, hình ảnh có sẵn trong thiết bị.

### 1.6. Kiến trúc và tổ chức ứng dụng

Ứng dụng được tổ chức theo hướng tách từng màn hình và chức năng thành các lớp riêng biệt để dễ quản lý và mở rộng. Một số thành phần chính của hệ thống gồm:

- [`MainActivity`](app/src/main/java/com/example/videoproject/MainActivity.java): màn hình chính, hiển thị danh sách video và cho phép truy cập các nhóm chức năng khác.
- [`VideoPlayerActivity`](app/src/main/java/com/example/videoproject/VideoPlayerActivity.java): màn hình phát video với đầy đủ các điều khiển chính.
- [`VideoDBHelper`](app/src/main/java/com/example/videoproject/VideoDBHelper.java): lớp quản lý cơ sở dữ liệu SQLite.
- [`PlaylistActivity`](app/src/main/java/com/example/videoproject/PlaylistActivity.java): quản lý danh sách playlist.
- [`PlaylistDetailActivity`](app/src/main/java/com/example/videoproject/PlaylistDetailActivity.java): hiển thị chi tiết các video trong từng playlist.
- [`OnlineVideoListActivity`](app/src/main/java/com/example/videoproject/OnlineVideoListActivity.java): quản lý và phát danh sách video online theo URL.
- [`ImageGalleryActivity`](app/src/main/java/com/example/videoproject/ImageGalleryActivity.java): hiển thị thư viện ảnh.
- [`VideoUtils`](app/src/main/java/com/example/videoproject/VideoUtils.java): lớp tiện ích hỗ trợ quét video và định dạng dữ liệu.

Kiến trúc này giúp chương trình dễ hiểu, dễ chia nhỏ công việc và thuận tiện khi bảo trì sau này.

---

## 4. Nội dung thay thế cho CHƯƠNG II

## CHƯƠNG II: PHÂN TÍCH VÀ XÂY DỰNG ỨNG DỤNG

### 2.1. Khảo sát yêu cầu hệ thống

Ứng dụng hướng đến người dùng cá nhân có nhu cầu xem video trên điện thoại Android. Từ nhu cầu thực tế, hệ thống cần đáp ứng các nhóm yêu cầu chính sau:

**Yêu cầu chức năng:**

- Hiển thị danh sách video có trong thiết bị.
- Cho phép chọn video để phát.
- Hỗ trợ sắp xếp danh sách video.
- Lưu lịch sử xem và tiếp tục xem.
- Đánh dấu video yêu thích.
- Tạo, sửa, xóa playlist.
- Thêm video vào playlist.
- Tạo bookmark tại thời điểm đang xem.
- Ghi chú nội dung gắn với từng mốc thời gian.
- Lưu và phát video online theo đường dẫn.
- Hỗ trợ xem ảnh trong thiết bị.

**Yêu cầu phi chức năng:**

- Giao diện dễ sử dụng, rõ ràng.
- Tốc độ phản hồi nhanh với dữ liệu cục bộ.
- Ứng dụng hoạt động ổn định trên điện thoại Android phổ biến.
- Dữ liệu cục bộ được lưu bền vững giữa các lần mở ứng dụng.
- Hạn chế lỗi phát sinh khi xoay màn hình hoặc thay đổi trạng thái phát video.

### 2.2. Phân tích chức năng

#### 2.2.1. Chức năng quản lý danh sách video

Khi ứng dụng khởi động, màn hình chính trong [`MainActivity`](app/src/main/java/com/example/videoproject/MainActivity.java) sẽ kiểm tra quyền truy cập bộ nhớ. Nếu người dùng đã cấp quyền, hệ thống tiến hành quét danh sách video từ thiết bị và hiển thị dưới dạng danh sách. Người dùng có thể nhấn vào từng video để mở trình phát.

Ngoài ra, hệ thống hỗ trợ các chế độ xem khác nhau như tất cả video, tiếp tục xem và video yêu thích. Điều này giúp người dùng truy cập nhanh đến nội dung cần thiết mà không phải tìm lại từ đầu.

#### 2.2.2. Chức năng phát video

Chức năng phát video được triển khai trong [`VideoPlayerActivity`](app/src/main/java/com/example/videoproject/VideoPlayerActivity.java). Màn hình này nhận danh sách đường dẫn video, tiêu đề video và vị trí video hiện tại để phát đúng nội dung người dùng đã chọn.

Các thao tác hỗ trợ trong trình phát gồm:

- Phát/tạm dừng video.
- Chuyển video tiếp theo hoặc trước đó.
- Tua nhanh/tua lùi 10 giây.
- Kéo thanh tiến trình để chuyển đến vị trí mong muốn.
- Xem toàn màn hình.
- Khóa điều khiển để tránh chạm nhầm.
- Điều chỉnh âm lượng và độ sáng bằng cử chỉ.
- Hiển thị thông tin video.
- Thay đổi tốc độ phát.
- Đặt hẹn giờ tắt.

Đây là phần trung tâm của toàn bộ ứng dụng, ảnh hưởng trực tiếp đến trải nghiệm người dùng.

#### 2.2.3. Chức năng lưu lịch sử xem

Ứng dụng lưu vị trí xem gần nhất của từng video vào cơ sở dữ liệu SQLite thông qua [`VideoDBHelper`](app/src/main/java/com/example/videoproject/VideoDBHelper.java). Khi người dùng mở lại video, hệ thống có thể hỏi có muốn xem tiếp từ vị trí cũ hay không. Chức năng này đặc biệt hữu ích với video dài như bài giảng, phim hoặc tài liệu học tập.

#### 2.2.4. Chức năng quản lý yêu thích

Người dùng có thể đánh dấu một video là yêu thích để dễ truy cập về sau. Danh sách yêu thích được lưu trong bảng riêng của SQLite. Từ đó, màn hình chính có thể lọc nhanh các video đã được người dùng quan tâm.

#### 2.2.5. Chức năng quản lý playlist

Playlist là nhóm video do người dùng tự tạo theo nhu cầu cá nhân, ví dụ: video học tập, video giải trí, video cần xem sau. Hệ thống cho phép:

- Tạo playlist mới.
- Đổi tên playlist.
- Xóa playlist.
- Thêm hoặc xóa video khỏi playlist.
- Xem chi tiết từng playlist.

Việc quản lý playlist giúp tăng tính cá nhân hóa và khiến ứng dụng thuận tiện hơn trong sử dụng hàng ngày.

#### 2.2.6. Chức năng bookmark và note

Trong khi xem video, người dùng có thể lưu bookmark tại một mốc thời gian quan trọng hoặc thêm ghi chú ngắn liên quan đến nội dung đang xem. Ví dụ, khi học qua video, người dùng có thể đánh dấu đoạn kiến thức quan trọng để quay lại nhanh sau đó. Đây là một điểm mở rộng hữu ích so với nhiều trình phát video cơ bản.

#### 2.2.7. Chức năng phát video online

Ứng dụng có màn hình quản lý video online để lưu các đường dẫn video từ Internet. Người dùng nhập tiêu đề, URL và mô tả, sau đó hệ thống lưu lại danh sách để phát khi cần. Chức năng này giúp ứng dụng không chỉ dùng cho video cục bộ mà còn hỗ trợ khai thác nội dung trực tuyến.

#### 2.2.8. Chức năng xem thư viện ảnh

Ngoài video, ứng dụng còn bổ sung thư viện ảnh giúp người dùng duyệt ảnh có trong thiết bị. Chức năng này mở rộng phạm vi đa phương tiện của ứng dụng, khiến sản phẩm trở nên hoàn chỉnh hơn.

### 2.3. Thiết kế giao diện

Giao diện ứng dụng được thiết kế bằng XML với định hướng đơn giản, trực quan và dễ thao tác trên điện thoại.

- Màn hình chính hiển thị danh sách video, thanh công cụ và các nút thao tác như sắp xếp, playlist, menu mở rộng.
- Màn hình phát video ưu tiên diện tích hiển thị nội dung, đồng thời đặt các nút điều khiển quan trọng ở vị trí thuận tiện.
- Màn hình playlist hiển thị danh sách playlist và số lượng video trong từng danh sách.
- Màn hình video online cho phép nhập URL và phát nội dung từ danh sách đã lưu.
- Màn hình thư viện ảnh hiển thị ảnh theo lưới để người dùng dễ quan sát.

Một số tệp giao diện tiêu biểu:

- [`activity_main.xml`](app/src/main/res/layout/activity_main.xml)
- [`activity_player.xml`](app/src/main/res/layout/activity_player.xml)
- [`activity_playlist.xml`](app/src/main/res/layout/activity_playlist.xml)
- [`activity_playlist_detail.xml`](app/src/main/res/layout/activity_playlist_detail.xml)
- [`activity_online_video_list.xml`](app/src/main/res/layout/activity_online_video_list.xml)
- [`activity_image_gallery.xml`](app/src/main/res/layout/activity_image_gallery.xml)

### 2.4. Thiết kế dữ liệu cục bộ

Cơ sở dữ liệu cục bộ của ứng dụng được quản lý bởi [`VideoDBHelper`](app/src/main/java/com/example/videoproject/VideoDBHelper.java). Các bảng chính gồm:

- **watch_history**: lưu lịch sử xem và vị trí xem gần nhất.
- **playlists**: lưu danh sách playlist.
- **playlist_videos**: lưu quan hệ giữa playlist và video.
- **favorites**: lưu các video yêu thích.
- **bookmarks**: lưu các mốc đánh dấu trong video.
- **notes**: lưu các ghi chú tại từng thời điểm trong video.
- **online_videos**: lưu video online do người dùng nhập.

Thiết kế này đủ gọn nhẹ cho một ứng dụng cục bộ, đồng thời đảm bảo có thể mở rộng thêm trong tương lai nếu cần đồng bộ dữ liệu hoặc bổ sung tài khoản người dùng.

### 2.5. Cài đặt các chức năng chính

#### a) Quét video trên thiết bị

Hệ thống sử dụng lớp tiện ích [`VideoUtils`](app/src/main/java/com/example/videoproject/VideoUtils.java) để truy xuất dữ liệu video từ bộ nhớ thiết bị. Sau khi quét, danh sách video được đưa vào `Adapter` để hiển thị trên màn hình chính.

#### b) Kiểm tra quyền truy cập

Do Android yêu cầu cấp quyền trước khi đọc nội dung đa phương tiện, ứng dụng thực hiện kiểm tra quyền đọc video trong [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml) và xử lý xin quyền trong [`MainActivity`](app/src/main/java/com/example/videoproject/MainActivity.java).

#### c) Phát video và lưu tiến trình xem

Trong quá trình phát video, ứng dụng theo dõi vị trí hiện tại và lưu định kỳ hoặc khi thoát khỏi trình phát. Nhờ đó người dùng có thể tiếp tục xem mà không cần tua lại thủ công.

#### d) Sắp xếp và lọc nội dung

Danh sách video hỗ trợ sắp xếp theo nhiều tiêu chí khác nhau như tên, ngày và dung lượng. Đây là chức năng nhỏ nhưng hữu ích khi số lượng video trên thiết bị lớn.

#### e) Quản lý dữ liệu người dùng

Các thao tác như thêm playlist, đánh dấu yêu thích, thêm bookmark, ghi chú hay lưu video online đều được xử lý qua SQLite. Điều này giúp dữ liệu được bảo toàn sau mỗi lần đóng ứng dụng.

---

## 5. Nội dung thay thế cho CHƯƠNG III

## CHƯƠNG III: KIỂM THỬ VÀ ĐÁNH GIÁ

### 3.1. Mục tiêu kiểm thử

Mục tiêu kiểm thử của bài tập lớn là đảm bảo ứng dụng hoạt động đúng chức năng, ổn định và mang lại trải nghiệm thuận tiện cho người dùng. Việc kiểm thử tập trung vào các nhóm chức năng chính như quét video, phát video, lưu lịch sử xem, quản lý playlist, phát video online và xem ảnh.

### 3.2. Các kịch bản kiểm thử

#### 3.2.1. Kiểm thử quyền truy cập bộ nhớ

- **Mục tiêu:** Xác nhận ứng dụng xử lý đúng khi người dùng cấp hoặc từ chối quyền truy cập.
- **Dữ liệu vào:** Mở ứng dụng lần đầu.
- **Kết quả mong đợi:** Nếu người dùng cấp quyền, ứng dụng tải danh sách video. Nếu từ chối, ứng dụng hiển thị thông báo phù hợp và không bị crash.

#### 3.2.2. Kiểm thử quét và hiển thị video

- **Mục tiêu:** Kiểm tra khả năng đọc video từ thiết bị.
- **Dữ liệu vào:** Thiết bị có sẵn nhiều video.
- **Kết quả mong đợi:** Danh sách video hiển thị đầy đủ, có tiêu đề, thời lượng và thông tin cơ bản.

#### 3.2.3. Kiểm thử phát video cục bộ

- **Mục tiêu:** Đảm bảo video phát bình thường.
- **Dữ liệu vào:** Chọn một video bất kỳ từ danh sách.
- **Kết quả mong đợi:** Video phát được, âm thanh và hình ảnh đồng bộ, các nút điều khiển hoạt động đúng.

#### 3.2.4. Kiểm thử lưu vị trí xem

- **Mục tiêu:** Kiểm tra khả năng tiếp tục xem.
- **Các bước:** Mở video, xem đến một thời điểm bất kỳ, thoát ra rồi mở lại.
- **Kết quả mong đợi:** Ứng dụng ghi nhớ vị trí xem trước đó và hỗ trợ tiếp tục xem.

#### 3.2.5. Kiểm thử playlist

- **Mục tiêu:** Đảm bảo người dùng có thể tạo và quản lý playlist.
- **Các bước:** Tạo playlist mới, thêm video vào playlist, mở playlist chi tiết.
- **Kết quả mong đợi:** Playlist được tạo thành công, dữ liệu lưu đúng và hiển thị chính xác.

#### 3.2.6. Kiểm thử yêu thích

- **Mục tiêu:** Kiểm tra chức năng đánh dấu yêu thích.
- **Kết quả mong đợi:** Video được thêm/xóa khỏi danh sách yêu thích đúng theo thao tác người dùng.

#### 3.2.7. Kiểm thử bookmark và note

- **Mục tiêu:** Kiểm tra khả năng thêm mốc đánh dấu và ghi chú.
- **Kết quả mong đợi:** Dữ liệu được lưu thành công, có thể truy xuất và hiển thị lại.

#### 3.2.8. Kiểm thử phát video online

- **Mục tiêu:** Kiểm tra khả năng phát video từ URL.
- **Dữ liệu vào:** Nhập một URL video trực tiếp hợp lệ.
- **Kết quả mong đợi:** Ứng dụng lưu được URL và phát được nếu nguồn nội dung tương thích với `MediaPlayer`.

#### 3.2.9. Kiểm thử giao diện và trải nghiệm người dùng

- **Mục tiêu:** Đánh giá khả năng thao tác của người dùng trên giao diện.
- **Kết quả mong đợi:** Bố cục rõ ràng, thao tác thuận tiện, các nút bấm dễ nhận biết và không gây nhầm lẫn.

### 3.3. Kết quả kiểm thử

Qua quá trình kiểm thử, ứng dụng đạt được các kết quả chính sau:

- Chức năng quét video trong thiết bị hoạt động đúng khi người dùng cấp quyền truy cập.
- Danh sách video hiển thị ổn định và hỗ trợ sắp xếp theo nhiều tiêu chí.
- Trình phát video hoạt động tốt với các thao tác phát, tạm dừng, tua nhanh, tua lùi, chuyển video và kéo thanh tiến trình.
- Chức năng lưu lịch sử xem và tiếp tục xem hoạt động đúng trong hầu hết các trường hợp cơ bản.
- Playlist, video yêu thích, bookmark và ghi chú đều lưu được vào SQLite.
- Chức năng phát video online hoạt động với các đường dẫn video trực tiếp tương thích.
- Giao diện ứng dụng nhìn chung dễ sử dụng, bố cục hợp lý và phù hợp với một ứng dụng học phần.

Bên cạnh đó, vẫn còn một số điểm cần cải thiện như khả năng xử lý nhiều định dạng link trực tuyến khác nhau, tối ưu giao diện trên nhiều cỡ màn hình và tăng cường kiểm soát lỗi khi nguồn video online không hợp lệ.

### 3.4. Đánh giá ưu điểm và hạn chế

**Ưu điểm:**

- Ứng dụng đáp ứng đúng mục tiêu chính là xem video trên Android.
- Có nhiều chức năng mở rộng hữu ích như playlist, yêu thích, bookmark, note và video online.
- Dữ liệu cục bộ được tổ chức tương đối rõ ràng.
- Giao diện đủ trực quan để người dùng thao tác nhanh.
- Có tính thực tiễn và khả năng mở rộng tiếp tục.

**Hạn chế:**

- Chưa hỗ trợ đầy đủ mọi loại nguồn phát trực tuyến.
- Chưa có đồng bộ dữ liệu lên đám mây.
- Chưa có cơ chế tài khoản người dùng.
- Giao diện vẫn có thể tối ưu thêm về tính hiện đại và responsive.
- Chưa có hệ thống kiểm thử tự động.

---

## 6. Nội dung thay thế cho CHƯƠNG IV

## CHƯƠNG IV: KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN

### 4.1. Kết luận

Sau quá trình nghiên cứu, phân tích, thiết kế và triển khai, đề tài đã xây dựng được một ứng dụng xem video trên Android đáp ứng được các yêu cầu cơ bản và một số chức năng mở rộng cần thiết. Ứng dụng cho phép quét video từ thiết bị, phát video bằng trình phát tích hợp, lưu lịch sử xem, tạo playlist, đánh dấu video yêu thích, thêm bookmark, ghi chú và phát video online theo URL.

Thông qua việc thực hiện đề tài, người thực hiện đã củng cố được nhiều kiến thức quan trọng trong lập trình ứng dụng di động như thiết kế giao diện bằng XML, xử lý Activity, sử dụng MediaPlayer, quản lý dữ liệu bằng SQLite, xin quyền truy cập tài nguyên thiết bị và tổ chức mã nguồn theo từng thành phần chức năng.

Đề tài không chỉ có ý nghĩa về mặt học tập mà còn có tính ứng dụng thực tế cao, vì nội dung phát video là nhu cầu rất phổ biến đối với người dùng điện thoại thông minh hiện nay.

### 4.2. Hạn chế

Mặc dù đã hoàn thành được các chức năng chính, ứng dụng vẫn còn một số hạn chế:

- Chưa hỗ trợ đầy đủ nhiều giao thức phát video trực tuyến nâng cao.
- Chưa xử lý tối ưu cho mọi loại định dạng video và nguồn mạng không ổn định.
- Chưa có cơ chế đồng bộ dữ liệu giữa nhiều thiết bị.
- Thiết kế giao diện còn đơn giản, chưa đạt mức hoàn thiện như các ứng dụng thương mại.
- Một số chức năng mới dừng ở mức cơ bản, có thể tiếp tục tối ưu về trải nghiệm người dùng.

### 4.3. Hướng phát triển

Trong thời gian tới, ứng dụng có thể được phát triển thêm theo các hướng sau:

- Nâng cấp giao diện người dùng theo phong cách hiện đại hơn.
- Hỗ trợ nhiều loại nguồn phát trực tuyến hơn như HLS (`.m3u8`) hoặc các nguồn stream phổ biến.
- Tích hợp trình phát mạnh hơn như ExoPlayer để tăng khả năng tương thích định dạng.
- Cho phép nhập URL linh hoạt hơn và tự nhận diện loại nguồn video.
- Đồng bộ playlist, lịch sử xem và bookmark thông qua tài khoản người dùng.
- Bổ sung tìm kiếm video, lọc theo thư mục hoặc phân loại theo thời lượng.
- Tối ưu hiệu năng trên thiết bị cấu hình thấp.
- Xây dựng bộ kiểm thử đầy đủ hơn để đánh giá chất lượng ứng dụng.

---

## 7. Đoạn mô tả ngắn về khả năng nhập link bất kỳ để xem video

Bạn có thể thêm đoạn này vào phần hướng phát triển hoặc phần đánh giá hệ thống:

Hiện tại, ứng dụng có thể phát video online khi người dùng nhập các đường dẫn trực tiếp tới tệp hoặc nguồn video tương thích với bộ giải mã của Android. Tuy nhiên, không phải mọi đường link trên Internet đều phát được. Các liên kết dạng trang web thông thường, liên kết YouTube, Facebook hoặc các trang có cơ chế bảo vệ nội dung thường không thể phát trực tiếp chỉ bằng `MediaPlayer`. Để hỗ trợ tốt hơn nhiều loại liên kết khác nhau, hệ thống cần bổ sung cơ chế phân tích URL, nhận diện loại nguồn phát và tích hợp trình phát mạnh hơn như ExoPlayer. Khi đó ứng dụng có thể mở rộng hỗ trợ các định dạng stream như HLS (`.m3u8`) hoặc một số nguồn không nhất thiết phải kết thúc bằng `.mp4`.

---

## 8. Gợi ý cách sửa trực tiếp trong file Word

Bạn nên dùng tài liệu này để thay thế các phần sau trong [`BaoCaoFinal.docx`](BaoCaoFinal.docx):

- Thay toàn bộ mục lục cũ bằng mục lục mới.
- Thay phần nội dung từ **Chương I** đến **Chương IV** đang bị lẫn đề tài website bán hàng.
- Giữ lại trang bìa, lời nói đầu nếu muốn, nhưng nên sửa tên ứng dụng cho thống nhất thành “ứng dụng xem video trên Android”.
- Nếu có ảnh giao diện thật của app Android, nên chèn vào thay cho các ảnh giao diện website cũ.

---

## 9. Kết luận sử dụng tài liệu này

Tài liệu [`BaoCaoFinal_bo_sung.md`](BaoCaoFinal_bo_sung.md) đóng vai trò như bản nội dung chuẩn hóa để bạn dán lại vào file Word. Sau khi cập nhật theo tài liệu này, báo cáo sẽ thống nhất với mã nguồn Android hiện có trong dự án và phù hợp hơn để đưa sang công cụ AI khác kiểm tra nội dung.

---

## 10. Nội dung cập nhật thêm sau khi rà soát và nâng cấp project

Sau khi rà soát lại mã nguồn trong [`VideoPlayerActivity.java`](app/src/main/java/com/example/videoproject/VideoPlayerActivity.java), ứng dụng đã được bổ sung thêm một số cải tiến trực tiếp liên quan tới trải nghiệm xem video. Phần này nên được chèn vào cuối báo cáo để thể hiện rõ sản phẩm không chỉ dừng ở mức phát video cơ bản mà đã có thêm các chức năng hỗ trợ học tập và giải trí thuận tiện hơn.

### 10.1. Các chức năng đã bổ sung

- **Chế độ lặp video (Repeat Mode):** trình phát hỗ trợ ba trạng thái gồm `Repeat Off`, `Repeat One` và `Repeat All`. Người dùng có thể lặp lại đúng video đang xem hoặc lặp toàn bộ danh sách phát hiện tại.
- **Hiển thị nhãn chất lượng video:** ứng dụng tự nhận diện độ phân giải video và hiển thị nhãn tương ứng như `LOW`, `SD`, `HD`, `FHD`, `2K`, `4K` ngay trên thanh điều khiển.
- **Hỗ trợ phụ đề rời định dạng `.srt`:** nếu trong cùng thư mục tồn tại tệp phụ đề cùng tên với video, trình phát có thể đọc nội dung phụ đề và hiển thị trực tiếp trên màn hình phát.
- **Nút bật/tắt phụ đề nhanh:** người dùng có thể chủ động bật hoặc tắt phụ đề bằng nút `CC` mà không cần mở màn hình cài đặt riêng.
- **Mở rộng hộp thoại thông tin video:** ngoài đường dẫn, thời lượng và độ phân giải, phần thông tin video hiện đã hiển thị thêm chất lượng suy luận và trạng thái có/không có phụ đề.
- **Tối ưu cập nhật tiến trình phát:** tốc độ cập nhật tiến trình và phụ đề được làm mượt hơn, giúp phần hiển thị thời gian và phụ đề bám sát video hơn trước.
- **Cải thiện hành vi khi phát hết video:** trong trường hợp bật lặp toàn danh sách, ứng dụng có thể quay vòng từ video cuối về video đầu thay vì dừng hẳn.

### 10.2. Ý nghĩa của các cập nhật mới

Các cập nhật trên giúp ứng dụng tiến gần hơn tới trải nghiệm của một trình phát đa phương tiện hoàn chỉnh:

- Chế độ lặp phù hợp khi người dùng học qua video, nghe nhạc nền hoặc xem lại một đoạn nhiều lần.
- Phụ đề giúp tăng khả năng tiếp cận nội dung, đặc biệt hữu ích với video học tập, video tiếng Anh hoặc nội dung cần theo dõi lời thoại chính xác.
- Nhãn chất lượng video giúp người dùng nhận biết nhanh mức độ rõ nét của nội dung đang phát.
- Hộp thoại thông tin chi tiết hơn giúp tăng tính minh bạch và hỗ trợ kiểm thử chức năng dễ dàng hơn.

### 10.3. Lưu ý kỹ thuật cần nêu rõ trong báo cáo

Để báo cáo chính xác về mặt kỹ thuật, nên mô tả rõ một số điểm sau:

- Chức năng **chất lượng video** hiện tại là **nhận diện và hiển thị mức chất lượng theo độ phân giải video đang phát**, chưa phải cơ chế chuyển đổi nhiều mức bitrate như YouTube hoặc các nền tảng streaming chuyên nghiệp.
- Chức năng **phụ đề** hiện hỗ trợ ở mức cơ bản với **tệp phụ đề rời `.srt`** đặt cùng thư mục và cùng tên với video.
- Ứng dụng **chưa hỗ trợ phụ đề nhúng sẵn bên trong file video**, chưa có chọn nhiều track subtitle và chưa hỗ trợ các định dạng phụ đề nâng cao hơn như ASS/SSA.
- Với video online, mức chất lượng hiển thị vẫn phụ thuộc vào nguồn video thực tế được phát bởi `MediaPlayer`.

### 10.4. Kịch bản kiểm thử nên bổ sung vào Chương III

Bạn có thể thêm các ca kiểm thử sau vào phần kiểm thử hệ thống:

1. **Kiểm thử lặp một video:** mở một video, bật `Repeat One`, chờ phát hết. Kết quả mong đợi là video tự phát lại từ đầu.
2. **Kiểm thử lặp toàn bộ danh sách:** mở danh sách có nhiều video, bật `Repeat All`, phát tới video cuối. Kết quả mong đợi là ứng dụng tự quay lại video đầu tiên.
3. **Kiểm thử phát kèm phụ đề:** đặt file video và file `.srt` cùng tên trong cùng thư mục, mở video rồi bật `CC`. Kết quả mong đợi là phụ đề hiển thị đúng theo mốc thời gian.
4. **Kiểm thử khi không có phụ đề:** mở video không có file `.srt` tương ứng. Kết quả mong đợi là nút phụ đề hiển thị trạng thái không khả dụng và ứng dụng không bị lỗi.
5. **Kiểm thử hiển thị chất lượng:** mở các video có độ phân giải khác nhau như 720p, 1080p. Kết quả mong đợi là nhãn chất lượng thay đổi đúng với độ phân giải video.
6. **Kiểm thử hộp thoại thông tin video:** mở mục thông tin video. Kết quả mong đợi là hiển thị đầy đủ đường dẫn, độ phân giải, chất lượng, thời lượng và trạng thái phụ đề.

### 10.5. Danh sách ảnh minh hoạ nên chèn vào báo cáo kèm tên file cụ thể

Để người làm file Word chèn ảnh nhanh và đúng, có thể dùng trực tiếp danh sách dưới đây theo đúng tên file trong thư mục [`imageworld`](imageworld):

| STT | Tên file ảnh | Nên chèn vào phần nào | Nội dung ảnh / chú thích gợi ý |
|---|---|---|---|
| 1 | [`01_danh_sach_video_dark.png`](imageworld/01_danh_sach_video_dark.png) | Chương II - giao diện màn hình chính | Giao diện màn hình chính hiển thị danh sách video trong bộ nhớ máy |
| 2 | [`02_menu_chinh_dark_mode_off.png`](imageworld/02_menu_chinh_dark_mode_off.png) | Chương II - menu chính / dark mode | Menu chính của ứng dụng ở chế độ tối |
| 3 | [`03_tao_playlist_dialog_dark.png`](imageworld/03_tao_playlist_dialog_dark.png) | Chương II - chức năng playlist | Hộp thoại tạo playlist mới |
| 4 | [`04_menu_sap_xep_video_dark.png`](imageworld/04_menu_sap_xep_video_dark.png) | Chương II - sắp xếp danh sách video | Chức năng sắp xếp video theo nhiều tiêu chí |
| 5 | [`05_menu_chinh_light_mode_on.png`](imageworld/05_menu_chinh_light_mode_on.png) | Chương II - giao diện / theme | Menu chính của ứng dụng ở chế độ sáng |
| 6 | [`06_playlist_chi_tiet_light.png`](imageworld/06_playlist_chi_tiet_light.png) | Chương II - playlist | Màn hình chi tiết playlist |
| 7 | [`07_chon_video_vao_playlist_1.png`](imageworld/07_chon_video_vao_playlist_1.png) | Chương II - playlist | Chọn video để thêm vào playlist |
| 8 | [`08_chon_video_vao_playlist_2.png`](imageworld/08_chon_video_vao_playlist_2.png) | Chương II - playlist | Tiếp tục thao tác thêm video vào playlist |
| 9 | [`09_gallery_grid.png`](imageworld/09_gallery_grid.png) | Chương II - thư viện ảnh | Giao diện thư viện ảnh dạng lưới |
| 10 | [`10_online_videos.png`](imageworld/10_online_videos.png) | Chương II - video online | Danh sách video online mẫu |
| 11 | [`11_player_fhd.png`](imageworld/11_player_fhd.png) | Chương II - trình phát video | Giao diện trình phát video tích hợp, có badge chất lượng |
| 12 | [`12_thong_tin_video.png`](imageworld/12_thong_tin_video.png) | Chương II - thông tin video | Hộp thoại thông tin video sau khi được mở rộng |
| 13 | [`13_sleep_timer.png`](imageworld/13_sleep_timer.png) | Chương II - tính năng phát video | Chức năng hẹn giờ tắt trong trình phát |
| 14 | [`14_notes_dialog.png`](imageworld/14_notes_dialog.png) | Chương II - bookmark / note | Hộp thoại ghi chú theo mốc thời gian |
| 15 | [`15_bookmark_dialog.png`](imageworld/15_bookmark_dialog.png) | Chương II - bookmark / note | Hộp thoại bookmark theo mốc thời gian |
| 16 | [`16_player_repeat_all.png`](imageworld/16_player_repeat_all.png) | Chương II - trình phát video | Chế độ lặp Repeat All trong trình phát |
| 17 | [`17_subtitle_lines_dialog.png`](imageworld/17_subtitle_lines_dialog.png) | Chương II - subtitle / AI subtitle | Danh sách các dòng subtitle đã nạp, có thể bấm để nhảy tới từng mốc |
| 18 | [`18_resume_playback_dialog.png`](imageworld/18_resume_playback_dialog.png) | Chương II hoặc Chương III - tiếp tục xem | Hộp thoại Resume Playback khi mở lại video đang xem dở |
| 19 | [`19_subtitle_options_dialog.png`](imageworld/19_subtitle_options_dialog.png) | Chương II - subtitle / AI subtitle | Hộp thoại Subtitle options với các tùy chọn bật/tắt subtitle và AI subtitle |

### 10.6. Hướng dẫn chèn ảnh vào file Word cho nhanh

Có thể giao cho người làm Word chèn theo thứ tự sau:

- **Nhóm giao diện chính và quản lý video:** chèn ảnh 1, 2, 4, 5.
- **Nhóm playlist:** chèn ảnh 3, 6, 7, 8.
- **Nhóm video online và thư viện ảnh:** chèn ảnh 9, 10.
- **Nhóm trình phát video:** chèn ảnh 11, 12, 13, 16, 18.
- **Nhóm bookmark / note:** chèn ảnh 14, 15.
- **Nhóm subtitle / AI subtitle:** chèn ảnh 17, 19.

Nếu cần làm báo cáo ngắn gọn nhưng vẫn đủ mạnh, nên ưu tiên tối thiểu các file sau:

- [`01_danh_sach_video_dark.png`](imageworld/01_danh_sach_video_dark.png)
- [`11_player_fhd.png`](imageworld/11_player_fhd.png)
- [`12_thong_tin_video.png`](imageworld/12_thong_tin_video.png)
- [`16_player_repeat_all.png`](imageworld/16_player_repeat_all.png)
- [`17_subtitle_lines_dialog.png`](imageworld/17_subtitle_lines_dialog.png)
- [`19_subtitle_options_dialog.png`](imageworld/19_subtitle_options_dialog.png)

Đây là bộ ảnh ngắn gọn nhưng thể hiện rõ nhất các phần nâng cấp chính của project.

### 10.7. Bổ sung tính năng tạo phụ đề tự động bằng speech-to-text online

Ngoài cơ chế đọc phụ đề rời `.srt` đặt cùng thư mục với video, ứng dụng hiện đã được mở rộng thêm khả năng **tạo phụ đề tự động từ âm thanh của video thông qua dịch vụ speech-to-text online**.

Cụ thể, hệ thống tích hợp dịch vụ **AssemblyAI** thông qua lớp [`AiSubtitleGenerator.java`](app/src/main/java/com/example/videoproject/AiSubtitleGenerator.java). Khi người dùng mở một **video cục bộ** nhưng chưa có phụ đề, nút phụ đề trong [`VideoPlayerActivity.java`](app/src/main/java/com/example/videoproject/VideoPlayerActivity.java) sẽ chuyển sang trạng thái `CC AI`. Người dùng có thể nhập API key của AssemblyAI, sau đó yêu cầu hệ thống tạo phụ đề tự động.

Luồng xử lý của tính năng này gồm các bước:

1. Lấy đường dẫn video cục bộ đang phát.
2. Tải tệp video lên AssemblyAI bằng kết nối Internet.
3. Gửi yêu cầu speech-to-text để nhận transcript có mốc thời gian.
4. Theo dõi tiến trình xử lý của dịch vụ cho tới khi transcript hoàn tất.
5. Gom các từ có timestamp thành các câu phụ đề theo định dạng `.srt`.
6. Lưu tệp phụ đề sinh ra vào thư mục riêng của ứng dụng.
7. Nạp lại phụ đề vừa tạo vào trình phát và cho phép hiển thị ngay trên màn hình video.

Cách triển khai này có ưu điểm là không cần tích hợp thư viện native phức tạp để tách audio hoặc chạy mô hình nhận diện giọng nói trực tiếp trên thiết bị. Nhờ vậy, kiến trúc của ứng dụng vẫn giữ được sự gọn nhẹ, phù hợp với project Java Android hiện tại.

### 10.8. Lưu ý kỹ thuật cần nêu rõ thêm trong báo cáo

Để phần mô tả chính xác hơn về mặt kỹ thuật, nên nêu rõ các giới hạn và điều kiện sử dụng của tính năng speech-to-text online:

- Tính năng chỉ hỗ trợ **video cục bộ trên thiết bị**, chưa áp dụng cho video online phát từ URL.
- Thiết bị cần có **kết nối Internet** ổn định trong quá trình upload và chờ transcript.
- Người dùng cần cung cấp **AssemblyAI API key** hợp lệ; chi phí sử dụng và quota phụ thuộc gói dịch vụ của nhà cung cấp.
- Thời gian tạo phụ đề phụ thuộc vào dung lượng video, tốc độ mạng và thời gian xử lý phía máy chủ.
- Tệp phụ đề được lưu trong **thư mục riêng của ứng dụng** thay vì ghi trực tiếp vào thư mục chứa video, nhằm tránh phát sinh thêm vấn đề về quyền ghi file.
- Khi video đã có phụ đề AI được tạo từ trước, trình phát có thể ưu tiên nạp lại tệp phụ đề đó khi mở lại cùng video.
- Đây là cơ chế **speech-to-text thật qua Internet**, nên chất lượng phụ đề phụ thuộc vào độ rõ của âm thanh đầu vào, ngôn ngữ nói và độ chính xác của dịch vụ nhận diện.

### 10.9. Kịch bản kiểm thử nên bổ sung thêm cho tính năng AI subtitle

Bạn có thể thêm các ca kiểm thử sau vào Chương III để mô tả rõ hơn phần mở rộng mới:

1. **Kiểm thử nhập API key:** mở tùy chọn subtitle AI, nhập API key hợp lệ. Kết quả mong đợi là key được lưu lại và có thể dùng cho lần tạo phụ đề tiếp theo.
2. **Kiểm thử tạo subtitle AI cho video cục bộ:** mở một video local chưa có `.srt`, chọn tạo phụ đề AI. Kết quả mong đợi là sau khi xử lý xong, phụ đề được sinh ra và hiển thị trên trình phát.
3. **Kiểm thử video online:** mở video online từ URL và thử dùng chức năng AI subtitle. Kết quả mong đợi là ứng dụng thông báo chỉ hỗ trợ video cục bộ.
4. **Kiểm thử mở lại video đã tạo subtitle:** sau khi tạo phụ đề thành công, thoát ra và mở lại cùng video. Kết quả mong đợi là phụ đề đã sinh trước đó được nạp lại nhanh chóng.
5. **Kiểm thử đổi video trong lúc AI đang xử lý:** bắt đầu tạo phụ đề cho video A rồi chuyển sang video B. Kết quả mong đợi là kết quả subtitle của video A không bị gán nhầm vào trạng thái của video B.
6. **Kiểm thử lỗi mạng hoặc API key sai:** ngắt mạng hoặc nhập API key không hợp lệ. Kết quả mong đợi là ứng dụng báo lỗi rõ ràng và không bị crash.

### 10.10. Ghi chú cuối về ảnh subtitle trong báo cáo

Hiện tại bộ ảnh đã đủ tốt để nộp báo cáo, vì đã có hai ảnh thể hiện rõ luồng subtitle:

- [`17_subtitle_lines_dialog.png`](imageworld/17_subtitle_lines_dialog.png): chứng minh subtitle đã được nạp và có thể duyệt từng dòng.
- [`19_subtitle_options_dialog.png`](imageworld/19_subtitle_options_dialog.png): chứng minh có phần quản lý subtitle và AI subtitle trong trình phát.

Nếu sau này muốn làm báo cáo đẹp hơn nữa, có thể bổ sung thêm **một ảnh subtitle overlay hiện trực tiếp trên video**. Tuy nhiên ảnh này hiện không còn là bắt buộc vì hai ảnh trên đã đủ làm bằng chứng chức năng.