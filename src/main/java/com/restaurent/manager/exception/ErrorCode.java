package com.restaurent.manager.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(500, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(400, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(400, "người dùng đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),
    EMAIL_EXIST(400, "Email đã tồn tại", HttpStatus.BAD_REQUEST),
    PHONENUMBER_EXIST(400, "Số điện thoại đã tồn tại", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(400, "Tên người dùng phải có ít nhất {min} ký tự", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(400, "Mật khẩu phải có từ 8 đến 16 ký tự bao gồm chữ số chữ hoa chữ thường và ký tự đặc biệt", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(404, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(401, "Chưa xác thực", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(403, "No permission", HttpStatus.FORBIDDEN),
    ROLE_EXISTED(400,"Vai trò đã tồn tại",HttpStatus.BAD_REQUEST),
    ROLE_NOT_EXISTED(400,"Vai trò không tồn tại",HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(400,"Mã không hợp lệ",HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(400,"Tên người dùng không thể để trống",HttpStatus.BAD_REQUEST),
    INVALID_PHONENUMBER(400,"số điện thoại không hợp lệ",HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(400,"Email không hợp lệ",HttpStatus.BAD_REQUEST),
    PASSWORD_INCORRECT(400,"Mật khẩu không đúng",HttpStatus.BAD_REQUEST),
    DISH_CATEGORY_EXIST(400,"Tên danh mục món ăn đã tồn tại",HttpStatus.BAD_REQUEST),
    RESTAURANT_NOT_EXISTED(404, "Nhà hàng không tồn tại", HttpStatus.NOT_FOUND),
    LIMITED_RESTAURANT(400,"Bạn chỉ có thể tạo một nhà hàng ",HttpStatus.BAD_REQUEST),
    RESTAURANT_NAME_EXISTED(400,"Tên nhà hàng đã tồn tại ",HttpStatus.BAD_REQUEST),
    FIELD_EMPTY(400,"Trường không thể trống",HttpStatus.BAD_REQUEST),
    NUMBER_CHAIRS_TOO_FEW(400,"Số ghế phải lớn hơn {min}", HttpStatus.BAD_REQUEST),
    NOT_EXIST(404,"Id không tồn tại",HttpStatus.BAD_REQUEST),
    GREATER_NUMBER(400,"Không thể đặt trường nhỏ hơn {value}",HttpStatus.BAD_REQUEST),
    DISH_NOT_FOUND(404,"Món ăn không tồn tại" , HttpStatus.NOT_FOUND ),
    INVALID_COMBO_PRICE(400, "Giá combo phải là số và lớn hơn 0", HttpStatus.BAD_REQUEST),
    INVALID_COMBO_DESCRIPTION(400, "Mô tả combo không được để trống", HttpStatus.BAD_REQUEST),
    INVALID_CUSTOMER_NAME(400, "Tên khách hàng không được để trống", HttpStatus.BAD_REQUEST),
    COMBO_NOT_EXISTED(400, "Combo không tồn tại", HttpStatus.NOT_FOUND),
    INVALID_COMBO_NAME(400, "Tên combo không được để trống", HttpStatus.BAD_REQUEST),
    PACKAGE_NOT_EXIST(404,"Gói không tồn tại",HttpStatus.BAD_REQUEST),
    TABLE_NAME_EXISTED(400,"Tên bàn đã tồn tại",HttpStatus.BAD_REQUEST),
    TABLE_TYPE_NULL(400,"Kiểu bàn không thể để trống",HttpStatus.BAD_REQUEST),
    AREA_NULL(400,"Khu vực không thể để trống",HttpStatus.BAD_REQUEST),
    NAME_NULL(400,"Tên không thể trống",HttpStatus.BAD_REQUEST),
    EMPLOYEE_NOT_EXIST(404,"Nhân viên không tồn tại",HttpStatus.BAD_REQUEST),
    CUSTOMER_NOT_EXIST(404,"Khách hàng không tồn tại",HttpStatus.BAD_REQUEST),
    INVALID_VAT_CODE(400,"Mã VAT không hợp lệ", HttpStatus.BAD_REQUEST),
    NOT_EMPTY(400,"Trường không thể trống",HttpStatus.BAD_REQUEST),
    EMAIL_NOT_EXIST(400, "Email không tồn tại",HttpStatus.BAD_REQUEST),
    INVALID_TAX_VALUE(400,"Giá trị thuế phải nằm trong khoảng từ {min} đến {max}",HttpStatus.BAD_REQUEST),
    MAX_AREA(400,"Bạn đã đạt giới hạn khu vực, vui lòng nâng cấp gói để có thể thêm", HttpStatus.BAD_REQUEST),
    RESTAURANT_ID_NULL(400,"id nhà hàng không thể là null", HttpStatus.BAD_REQUEST),
    MAX_TABLE(400, "Bạn không thể tạo thêm bàn vì đã vượt qua giới hạn cho phép của gói, nâng cấp gói để tạo bàn.", HttpStatus.BAD_REQUEST),
    BOOKED_TABLE(400, "Bàn đã được đặt trước, vui lòng đặt bàn khác", HttpStatus.BAD_REQUEST),
    BOOKED_DATE_INVALID(400, "Vui lòng chọn ngày lớn hơn ngày hiện tại", HttpStatus.BAD_REQUEST),
    TIME_INVALID(400, "Vui lòng chọn thời gian lớn hơn thời gian hiện tại", HttpStatus.BAD_REQUEST),
    NOT_NULL(400, "Trường không thể để null", HttpStatus.BAD_REQUEST),
    MIN_VALUE(400, "Must be greater than {min}", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_NULL(400,"ID tài khoản không phải là null", HttpStatus.BAD_REQUEST),
    RESTAURANT_NOT_NULL(400,"Mã nhà hàng không phải là null",HttpStatus.BAD_REQUEST),
    PACKAGE_NOT_NULL(400,"Gói id không phải là null", HttpStatus.BAD_REQUEST),
    MONTHS_NOT_NULL(400,"Tháng không thể để trống", HttpStatus.BAD_REQUEST),
    EMAIL_PHONE_NUMBER_INVAILD(400, "Số điện thoại hoặc email không tồn tại", HttpStatus.BAD_REQUEST),
    INVALID_CODE(400,"Thực đơn không tồn tại", HttpStatus.BAD_REQUEST),
    POINT_INVALID(400, "Số điểm sử dụng đã vượt quá số điểm còn dư", HttpStatus.BAD_REQUEST),
    QUANTITY_INVALID(400, "Quantity phải lớn hơn hoặc bằng 1", HttpStatus.BAD_REQUEST),
    INVALID(400, "vui lòng nhập đầy đủ thông tin", HttpStatus.BAD_REQUEST),
    TABLE_NOT_FREE(400, "Bàn đang có người sử dụng không thể vào bàn", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_EXIST(400, "Tài khoản không tồn tại", HttpStatus.BAD_REQUEST),
    NAME_EMPTY(400,"Name không thể để trống", HttpStatus.BAD_REQUEST),
    STATUS_NOT_NULL(400, "Trạng thái món ăn không thể để trống", HttpStatus.BAD_REQUEST),
    DESCRIPTION_NOT_NULL(400, "Miêu tả không thể để trống", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_NULL(400, "Thể loại món ăn không thể để trống", HttpStatus.BAD_REQUEST),
    IMAGE_NOT_NULL(400, "Ảnh không thể để trống", HttpStatus.BAD_REQUEST),
    UNIT_NOT_NULL(400, "Unit không thể để trống", HttpStatus.BAD_REQUEST),
    VAT_INVALID(400,"Vui lòng cập nhật thông tin thuế !", HttpStatus.BAD_REQUEST),
    CUSTOMER_NAME_INVALID(400, "Tên khách hàng không thể vượt quá {max} kí tự", HttpStatus.BAD_REQUEST),
    USERNAME_NAME_INVALID(400,"Tên tài khoản nhân viên không thể vượt quá {max} kí tự",HttpStatus.BAD_REQUEST),
    EMPLOYEE_NAME_INVALID(400,"Tên nhân viên không thể vượt quá {max} kí tự",HttpStatus.BAD_REQUEST),
    ADDRESS_INVALID(400, "địa chỉ dưới 100 kí tự",HttpStatus.BAD_REQUEST),
    NOT_TODAY(400, "Chưa đến thời gian để nhận bàn", HttpStatus.BAD_REQUEST)
    ;



    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
