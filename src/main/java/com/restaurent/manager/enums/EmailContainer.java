package com.restaurent.manager.enums;


public class EmailContainer {
    public static String formMailBody(String otp){
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Verification Code</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            width: 100%;\n" +
                "            height: 100%;\n" +
                "            padding: 0;\n" +
                "            margin: 0;\n" +
                "            background-color: #FAFAFA;\n" +
                "        }\n" +
                "        .es-wrapper {\n" +
                "            width: 100%;a\n" +
                "            height: 100%;\n" +
                "            background-color: #FAFAFA;\n" +
                "        }\n" +
                "        .es-header, .es-content, .es-footer {\n" +
                "            width: 100%;\n" +
                "            background-color: transparent;\n" +
                "        }\n" +
                "        .es-header-body, .es-content-body, .es-footer-body {\n" +
                "            width: 600px;\n" +
                "            background-color: #FFFFFF;\n" +
                "            margin: 0 auto;\n" +
                "        }\n" +
                "        .es-menu td {\n" +
                "            padding: 10px;\n" +
                "            font-family: verdana, geneva, sans-serif;\n" +
                "            font-size: 14px;\n" +
                "            color: #096b81;\n" +
                "        }\n" +
                "        .es-otp {\n" +
                "            background-color: #08565c;\n" +
                "            color: #FFFFFF;\n" +
                "            font-size: 20px;\n" +
                "            padding: 10px 30px;\n" +
                "            border-radius: 6px;\n" +
                "            display: inline-block;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <table class=\"es-wrapper\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "        <tr>\n" +
                "            <td>\n" +
                "                \n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                </table>\n" +
                "                <table class=\"es-content\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                    <tr>\n" +
                "                        <td>\n" +
                "                            <table class=\"es-content-body\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\" >\n" +
                "                                        <img src=\"http://res.cloudinary.com/dggciohw8/image/upload/v1724352974/l9d72ppswyclypebn8me.jpg\" alt=\"Verify Logo\" height=\"220\">\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\" style=\"padding-bottom: 15px;\">\n" +
                "                                        <h1 style=\"font-family: 'Trebuchet MS', sans-serif; color: #096b81; font-size: 46px; margin: 0;\">Mã xác nhận</h1>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\" style=\"padding: 5px 40px;\">\n" +
                "                                        <p style=\"font-family: Arial, sans-serif; color: #333333; font-size: 14px; margin: 0;\">Bạn nhận được tin nhắn này vì địa chỉ email của bạn đã được đăng ký với trang web của chúng tôi. Vui lòng sử dụng OTP bên dưới để xác minh địa chỉ email của bạn và xác nhận rằng bạn là chủ sở hữu của tài khoản này.</p>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\" style=\"padding: 10px 0;\">\n" +
                "                                        <p style=\"font-family: Arial, sans-serif; color: #333333; font-size: 14px; margin: 0;\">Nếu bạn chưa đăng ký với chúng tôi, vui lòng bỏ qua email này.</p>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\" style=\"padding: 10px 0;\">\n" +
                "                                        <div class=\"es-otp\">OTP: "+ otp +"</div>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\" style=\"padding: 5px 40px;\">\n" +
                "                                        <p style=\"font-family: Arial, sans-serif; color: #333333; font-size: 14px; margin: 0;\">Sau khi xác nhận, email này sẽ được liên kết duy nhất với tài khoản của bạn.</p>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                <tr>\n" +
                "                                   \n" +
                "                                </tr>\n" +
                "                            </table>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                </table>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "</body>\n" +
                "</html>\n";
    }
    public static String formMailForgot(String password){
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Verification Code</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            width: 100%;\n" +
                "            height: 100%;\n" +
                "            padding: 0;\n" +
                "            margin: 0;\n" +
                "            background-color: #FAFAFA;\n" +
                "        }\n" +
                "        .es-wrapper {\n" +
                "            width: 100%;a\n" +
                "            height: 100%;\n" +
                "            background-color: #FAFAFA;\n" +
                "        }\n" +
                "        .es-header, .es-content, .es-footer {\n" +
                "            width: 100%;\n" +
                "            background-color: transparent;\n" +
                "        }\n" +
                "        .es-header-body, .es-content-body, .es-footer-body {\n" +
                "            width: 600px;\n" +
                "            background-color: #FFFFFF;\n" +
                "            margin: 0 auto;\n" +
                "        }\n" +
                "        .es-menu td {\n" +
                "            padding: 10px;\n" +
                "            font-family: verdana, geneva, sans-serif;\n" +
                "            font-size: 14px;\n" +
                "            color: #096b81;\n" +
                "        }\n" +
                "        .es-otp {\n" +
                "            background-color: #08565c;\n" +
                "            color: #FFFFFF;\n" +
                "            font-size: 20px;\n" +
                "            padding: 10px 30px;\n" +
                "            border-radius: 6px;\n" +
                "            display: inline-block;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <table class=\"es-wrapper\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "        <tr>\n" +
                "            <td>\n" +
                "                \n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                </table>\n" +
                "                <table class=\"es-content\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                    <tr>\n" +
                "                        <td>\n" +
                "                            <table class=\"es-content-body\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\" >\n" +
                "                                        <img src=\"http://res.cloudinary.com/dggciohw8/image/upload/v1724352974/l9d72ppswyclypebn8me.jpg\" alt=\"Verify Logo\" height=\"220\">\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\" style=\"padding-bottom: 15px;\">\n" +
                "                                        <h1 style=\"font-family: 'Trebuchet MS', sans-serif; color: #096b81; font-size: 46px; margin: 0;\">Mã xác nhận</h1>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\" style=\"padding: 5px 40px;\">\n" +
                "                                        <p style=\"font-family: Arial, sans-serif; color: #333333; font-size: 14px; margin: 0;\">Bạn nhận được tin nhắn này vì bạn quên mật khẩu cho địa chỉ email của mình. Vui lòng sử dụng mật khẩu mới bên dưới để xác minh địa chỉ email của bạn và xác nhận rằng bạn là chủ sở hữu của email này.</p>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\" style=\"padding: 10px 0;\">\n" +
                "                                        <p style=\"font-family: Arial, sans-serif; color: #333333; font-size: 14px; margin: 0;\">Nếu bạn chưa đăng ký với chúng tôi, vui lòng bỏ qua email này.</p>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\" style=\"padding: 10px 0;\">\n" +
                "                                        <div class=\"es-otp\">Mật khẩu mới: "+ password +"</div>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\" style=\"padding: 5px 40px;\">\n" +
                "                                        <p style=\"font-family: Arial, sans-serif; color: #333333; font-size: 14px; margin: 0;\">Hãy nhập nhập mật khẩu mới, để đăng nhập vào tài khoản của bạn.</p>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                <tr>\n" +
                "                                   \n" +
                "                                </tr>\n" +
                "                            </table>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                </table>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "</body>\n" +
                "</html>\n";
    }

    public static String formMailPackage(String packageUpdate){
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Verification Code</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            width: 100%;\n" +
                "            height: 100%;\n" +
                "            padding: 0;\n" +
                "            margin: 0;\n" +
                "            background-color: #FAFAFA;\n" +
                "        }\n" +
                "        .es-wrapper {\n" +
                "            width: 100%;a\n" +
                "            height: 100%;\n" +
                "            background-color: #FAFAFA;\n" +
                "        }\n" +
                "        .es-header, .es-content, .es-footer {\n" +
                "            width: 100%;\n" +
                "            background-color: transparent;\n" +
                "        }\n" +
                "        .es-header-body, .es-content-body, .es-footer-body {\n" +
                "            width: 600px;\n" +
                "            background-color: #FFFFFF;\n" +
                "            margin: 0 auto;\n" +
                "        }\n" +
                "        .es-menu td {\n" +
                "            padding: 10px;\n" +
                "            font-family: verdana, geneva, sans-serif;\n" +
                "            font-size: 14px;\n" +
                "            color: #096b81;\n" +
                "        }\n" +
                "        .es-otp {\n" +
                "            background-color: #08565c;\n" +
                "            color: #FFFFFF;\n" +
                "            font-size: 20px;\n" +
                "            padding: 10px 30px;\n" +
                "            border-radius: 6px;\n" +
                "            display: inline-block;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <table class=\"es-wrapper\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "        <tr>\n" +
                "            <td>\n" +
                "                \n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                </table>\n" +
                "                <table class=\"es-content\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                    <tr>\n" +
                "                        <td>\n" +
                "                            <table class=\"es-content-body\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\" >\n" +
                "                                        <img src=\"http://res.cloudinary.com/dggciohw8/image/upload/v1724352974/l9d72ppswyclypebn8me.jpg\" alt=\"Verify Logo\" height=\"220\">\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\" style=\"padding-bottom: 15px;\">\n" +
                "                                        <h1 style=\"font-family: 'Trebuchet MS', sans-serif; color: #096b81; font-size: 46px; margin: 0;\">Nâng cấp gói thành công</h1>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\" style=\"padding: 5px 40px;\">\n" +
                "                                        <p style=\"font-family: Arial, sans-serif; color: #333333; font-size: 14px; margin: 0;\">Bạn nhận được tin nhắn này vì bạn đã nâng cấp gói "+ packageUpdate +" thành công. VietKitchen chúc bạn có một trải nghiệm thật tốt với dịch vụ của chúng tôi..</p>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\" style=\"padding: 10px 0;\">\n" +
                "                                        <p style=\"font-family: Arial, sans-serif; color: #333333; font-size: 14px; margin: 0;\">Mọi thắc mắc cần hỗ trợ vui lòng liên hệ với số điện thoại: 0888637937</p>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\" style=\"padding: 10px 0;\">\n" +
                "                                        <div class=\"es-otp\"></div>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\" style=\"padding: 5px 40px;\">\n" +
                "                                        <p style=\"font-family: Arial, sans-serif; color: #333333; font-size: 14px; margin: 0;\">Chúng tôi xin chân thành cảm ơn và sẽ cố gắng đem đến những gì tốt nhât dành cho quý khách..</p>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                                <tr>\n" +
                "                                   \n" +
                "                                </tr>\n" +
                "                            </table>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                </table>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "</body>\n" +
                "</html>\n";
    }
}
