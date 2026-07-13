$env:Path = "$env:Path;$env:LOCALAPPDATA\OfficeCLI"
$f = "d:\Android_devpro\Android\Event_Ticket_Booking_App\EventPass_Presentation.pptx"

# Xoa file cu, tao moi
officecli close $f 2>$null
Remove-Item $f -Force 2>$null
officecli create $f

# === SLIDE 1 - TRANG BIA ===
officecli add $f / --type slide --prop background=F5EDDE
officecli add $f '/slide[1]' --type shape --prop text="EventPass" --prop x=2cm --prop y=4cm --prop w=22cm --prop h=3cm --prop size=54 --prop color=C0392B --prop bold=true --prop font=Arial
officecli add $f '/slide[1]' --type shape --prop text="H??? th???ng ?????t V?? S??? ki???n Th??ng minh" --prop x=2cm --prop y=8cm --prop w=22cm --prop h=2cm --prop size=24 --prop color=5D4037 --prop font=Arial
officecli add $f '/slide[1]' --type shape --prop text="T??c gi???: Ho??ng H???i  |  Tr?????ng: HUMG  |  2026" --prop x=2cm --prop y=12cm --prop w=22cm --prop h=1.5cm --prop size=16 --prop color=8D6E63 --prop font=Arial

# === SLIDE 2 - TONG QUAN ===
officecli add $f / --type slide --prop background=FFF8F0
officecli add $f '/slide[2]' --type shape --prop text="T???ng Quan D??? ??n" --prop x=1.5cm --prop y=1cm --prop w=23cm --prop h=2cm --prop size=36 --prop color=C0392B --prop bold=true --prop font=Arial
officecli add $f '/slide[2]' --type shape --prop text="EventPass l?? ???ng d???ng Android hi???n ?????i cung c???p gi???i ph??p to??n di???n cho vi???c ?????t v?? s??? ki???n, qu???n l?? h??a ????n v?? check-in chuy??n nghi???p b???ng m?? QR." --prop x=1.5cm --prop y=3.5cm --prop w=23cm --prop h=3cm --prop size=18 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[2]' --type shape --prop text="N???n t???ng: Android (Java) + Google Firebase" --prop x=2cm --prop y=7cm --prop w=22cm --prop h=1.2cm --prop size=16 --prop color=6D4C41 --prop font=Arial
officecli add $f '/slide[2]' --type shape --prop text="?????i t?????ng: Ng?????i mua v?? & Qu???n tr??? vi??n s??? ki???n" --prop x=2cm --prop y=8.5cm --prop w=22cm --prop h=1.2cm --prop size=16 --prop color=6D4C41 --prop font=Arial
officecli add $f '/slide[2]' --type shape --prop text="M???c ti??u: S??? h??a to??n b??? quy tr??nh ?????t v?? v?? check-in" --prop x=2cm --prop y=10cm --prop w=22cm --prop h=1.2cm --prop size=16 --prop color=6D4C41 --prop font=Arial

# === SLIDE 3 - TINH NANG USER 1/2 ===
officecli add $f / --type slide --prop background=F5EDDE
officecli add $f '/slide[3]' --type shape --prop text="T??nh N??ng Ng?????i D??ng (1/2)" --prop x=1.5cm --prop y=1cm --prop w=23cm --prop h=2cm --prop size=32 --prop color=C0392B --prop bold=true --prop font=Arial
officecli add $f '/slide[3]' --type shape --prop text="Kh??m ph?? s??? ki???n: Duy???t v?? t??m ki???m c??c s??? ki???n n???i b???t, workshop, h???i th???o v???i h??nh ???nh ch???t l?????ng cao." --prop x=2cm --prop y=4cm --prop w=22cm --prop h=1.5cm --prop size=16 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[3]' --type shape --prop text="?????t v?? nhanh ch??ng: Quy tr??nh mua v?? t???i gi???n, t??ch h???p Voucher gi???m gi??." --prop x=2cm --prop y=6cm --prop w=22cm --prop h=1.5cm --prop size=16 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[3]' --type shape --prop text="H??a ????n chi ti???t: ????n gi??, gi???m gi??, m?? giao d???ch, ph????ng th???c thanh to??n ?????y ?????." --prop x=2cm --prop y=8cm --prop w=22cm --prop h=1.5cm --prop size=16 --prop color=4E342E --prop font=Arial

# === SLIDE 4 - TINH NANG USER 2/2 ===
officecli add $f / --type slide --prop background=FFF8F0
officecli add $f '/slide[4]' --type shape --prop text="T??nh N??ng Ng?????i D??ng (2/2)" --prop x=1.5cm --prop y=1cm --prop w=23cm --prop h=2cm --prop size=32 --prop color=C0392B --prop bold=true --prop font=Arial
officecli add $f '/slide[4]' --type shape --prop text="V?? ??i???n t??? QR Code: M?? QR t??? ?????ng hi???n th??? 24 gi??? tr?????c s??? ki???n (Logic Tr?? ho??n K??ch ho???t)." --prop x=2cm --prop y=4cm --prop w=22cm --prop h=1.5cm --prop size=16 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[4]' --type shape --prop text="H??? th???ng Th??nh vi??n EXP: T??ch l??y ??i???m ????? th??ng h???ng ?????ng, B???c, V??ng, VIP." --prop x=2cm --prop y=6cm --prop w=22cm --prop h=1.5cm --prop size=16 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[4]' --type shape --prop text="L???ch s??? giao d???ch: Theo d??i chi ti??u, qu???n l?? y??u c???u ho??n ti???n d??? d??ng." --prop x=2cm --prop y=8cm --prop w=22cm --prop h=1.5cm --prop size=16 --prop color=4E342E --prop font=Arial

# === SLIDE 5 - TINH NANG ADMIN ===
officecli add $f / --type slide --prop background=F5EDDE
officecli add $f '/slide[5]' --type shape --prop text="T??nh N??ng Qu???n Tr??? Vi??n" --prop x=1.5cm --prop y=1cm --prop w=23cm --prop h=2cm --prop size=32 --prop color=C0392B --prop bold=true --prop font=Arial
officecli add $f '/slide[5]' --type shape --prop text="Tr??nh qu??t QR Cao c???p: Giao di???n t??y ch???nh v???i Hi???u ???ng Laser v?? ????n flash." --prop x=2cm --prop y=4cm --prop w=22cm --prop h=1.5cm --prop size=16 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[5]' --type shape --prop text="Check-in T???c th??: Truy xu???t d??? li???u th???i gian th???c ngay khi qu??t." --prop x=2cm --prop y=6cm --prop w=22cm --prop h=1.5cm --prop size=16 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[5]' --type shape --prop text="Qu???n l?? ????n h??ng: Ph?? duy???t/t??? ch???i ho??n ti???n, qu???n l?? kho v??." --prop x=2cm --prop y=8cm --prop w=22cm --prop h=1.5cm --prop size=16 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[5]' --type shape --prop text="Th???ng k??: Ph??n t??ch chi ti??u v?? b??o c??o hi???u su???t s??? ki???n." --prop x=2cm --prop y=10cm --prop w=22cm --prop h=1.5cm --prop size=16 --prop color=4E342E --prop font=Arial

# === SLIDE 6 - KIEN TRUC ===
officecli add $f / --type slide --prop background=FFF8F0
officecli add $f '/slide[6]' --type shape --prop text="Ki???n Tr??c H??? Th???ng" --prop x=1.5cm --prop y=1cm --prop w=23cm --prop h=2cm --prop size=32 --prop color=C0392B --prop bold=true --prop font=Arial
officecli add $f '/slide[6]' --type shape --prop text="M?? h??nh: MVC + ViewModel + LiveData" --prop x=2cm --prop y=3.5cm --prop w=22cm --prop h=1.5cm --prop size=20 --prop color=C0392B --prop bold=true --prop font=Arial
officecli add $f '/slide[6]' --type shape --prop text="Model: C??c l???p d??? li???u (User, Event, Booking, Review, Voucher)" --prop x=2cm --prop y=5.5cm --prop w=22cm --prop h=1.2cm --prop size=16 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[6]' --type shape --prop text="View: 59 giao di???n XML + Material Components 3" --prop x=2cm --prop y=7cm --prop w=22cm --prop h=1.2cm --prop size=16 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[6]' --type shape --prop text="Controller: 7 l???p x??? l?? logic nghi???p v??? v???i Firebase" --prop x=2cm --prop y=8.5cm --prop w=22cm --prop h=1.2cm --prop size=16 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[6]' --type shape --prop text="ViewModel + LiveData: Qu???n l?? tr???ng th??i giao di???n reactive" --prop x=2cm --prop y=10cm --prop w=22cm --prop h=1.2cm --prop size=16 --prop color=4E342E --prop font=Arial

# === SLIDE 7 - CONG NGHE ===
officecli add $f / --type slide --prop background=F5EDDE
officecli add $f '/slide[7]' --type shape --prop text="C??ng Ngh??? S??? D???ng" --prop x=1.5cm --prop y=1cm --prop w=23cm --prop h=2cm --prop size=32 --prop color=C0392B --prop bold=true --prop font=Arial
officecli add $f '/slide[7]' --type shape --prop text="Java (Android SDK 36) - Ng??n ng??? l???p tr??nh ch??nh" --prop x=2cm --prop y=3.5cm --prop w=22cm --prop h=1.2cm --prop size=16 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[7]' --type shape --prop text="Firebase BOM 34.15.0 - Auth, Firestore, Storage, Messaging" --prop x=2cm --prop y=5cm --prop w=22cm --prop h=1.2cm --prop size=16 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[7]' --type shape --prop text="ZXing Embedded 4.3.0 - Qu??t v?? t???o m?? QR" --prop x=2cm --prop y=6.5cm --prop w=22cm --prop h=1.2cm --prop size=16 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[7]' --type shape --prop text="Material Components 3 + Glide 4.16.0 - Giao di???n hi???n ?????i" --prop x=2cm --prop y=8cm --prop w=22cm --prop h=1.2cm --prop size=16 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[7]' --type shape --prop text="Lottie 6.1.0 - Hi???u ???ng ?????ng chuy??n nghi???p" --prop x=2cm --prop y=9.5cm --prop w=22cm --prop h=1.2cm --prop size=16 --prop color=4E342E --prop font=Arial

# === SLIDE 8 - API & DATABASE ===
officecli add $f / --type slide --prop background=FFF8F0
officecli add $f '/slide[8]' --type shape --prop text="API & C?? S??? D??? Li???u" --prop x=1.5cm --prop y=1cm --prop w=23cm --prop h=2cm --prop size=32 --prop color=C0392B --prop bold=true --prop font=Arial
officecli add $f '/slide[8]' --type shape --prop text="39 API chia th??nh 7 nh??m ch???c n??ng" --prop x=2cm --prop y=3.5cm --prop w=22cm --prop h=1.5cm --prop size=20 --prop color=C0392B --prop bold=true --prop font=Arial
officecli add $f '/slide[8]' --type shape --prop text="X??c th???c & Ng?????i d??ng (7)  |  S??? ki???n (8)  |  ?????t v?? (6)" --prop x=2cm --prop y=5.5cm --prop w=22cm --prop h=1.2cm --prop size=16 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[8]' --type shape --prop text="????nh gi?? (5)  |  Y??u th??ch (3)  |  Voucher (7)  |  C???u h??nh (3)" --prop x=2cm --prop y=7cm --prop w=22cm --prop h=1.2cm --prop size=16 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[8]' --type shape --prop text="Firestore: users, events, bookings, reviews, favorites, system_vouchers" --prop x=2cm --prop y=9cm --prop w=22cm --prop h=1.2cm --prop size=14 --prop color=8D6E63 --prop font=Arial

# === SLIDE 9 - GIAO DIEN ===
officecli add $f / --type slide --prop background=F5EDDE
officecli add $f '/slide[9]' --type shape --prop text="Giao Di???n Ng?????i D??ng" --prop x=1.5cm --prop y=1cm --prop w=23cm --prop h=2cm --prop size=32 --prop color=C0392B --prop bold=true --prop font=Arial
officecli add $f '/slide[9]' --type shape --prop text="59 giao di???n XML - Thi???t k??? to??n di???n" --prop x=2cm --prop y=3.5cm --prop w=22cm --prop h=1.5cm --prop size=20 --prop color=C0392B --prop bold=true --prop font=Arial
officecli add $f '/slide[9]' --type shape --prop text="25 Activity  |  4 Fragment  |  13 RecyclerView  |  8 Dialog" --prop x=2cm --prop y=5.5cm --prop w=22cm --prop h=1.2cm --prop size=16 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[9]' --type shape --prop text="Material Design 3 - Giao di???n hi???n ?????i, nh???t qu??n" --prop x=2cm --prop y=7cm --prop w=22cm --prop h=1.2cm --prop size=16 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[9]' --type shape --prop text="H??? tr??? Dark Mode v?? Lottie Animations" --prop x=2cm --prop y=8.5cm --prop w=22cm --prop h=1.2cm --prop size=16 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[9]' --type shape --prop text="Tr??nh qu??t QR t??y ch???nh v???i hi???u ???ng Laser" --prop x=2cm --prop y=10cm --prop w=22cm --prop h=1.2cm --prop size=16 --prop color=4E342E --prop font=Arial

# === SLIDE 10 - KET LUAN ===
officecli add $f / --type slide --prop background=FFF8F0
officecli add $f '/slide[10]' --type shape --prop text="K???t Lu???n & H?????ng Ph??t Tri???n" --prop x=1.5cm --prop y=1cm --prop w=23cm --prop h=2cm --prop size=32 --prop color=C0392B --prop bold=true --prop font=Arial
officecli add $f '/slide[10]' --type shape --prop text="EventPass ???? ho??n th??nh m???c ti??u s??? h??a quy tr??nh ?????t v?? v?? check-in s??? ki???n v???i h??? th???ng ?????y ????? t??nh n??ng cho c??? ng?????i d??ng v?? qu???n tr??? vi??n." --prop x=2cm --prop y=3.5cm --prop w=22cm --prop h=2.5cm --prop size=18 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[10]' --type shape --prop text="H?????ng ph??t tri???n t????ng lai:" --prop x=2cm --prop y=6.5cm --prop w=22cm --prop h=1.5cm --prop size=20 --prop color=C0392B --prop bold=true --prop font=Arial
officecli add $f '/slide[10]' --type shape --prop text="T??ch h???p thanh to??n tr???c tuy???n (VNPay, MoMo, ZaloPay)" --prop x=2cm --prop y=8.5cm --prop w=22cm --prop h=1.2cm --prop size=16 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[10]' --type shape --prop text="Push Notification nh???c nh??? s??? ki???n t??? ?????ng" --prop x=2cm --prop y=10cm --prop w=22cm --prop h=1.2cm --prop size=16 --prop color=4E342E --prop font=Arial
officecli add $f '/slide[10]' --type shape --prop text="AI ????? xu???t s??? ki???n c?? nh??n h??a | M??? r???ng iOS v?? Web" --prop x=2cm --prop y=11.5cm --prop w=22cm --prop h=1.2cm --prop size=16 --prop color=4E342E --prop font=Arial

# View & Close
officecli view $f outline
officecli close $f
