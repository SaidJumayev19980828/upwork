package com.nasnav.payments;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestController
@RequestMapping("/nasnav")
public class PaymentController {


    @PostMapping(value = "/checkout/order/{order_id}")
    public ResponseEntity<?> checkoutUsingQNBGateway(@PathVariable(name = "order_id") String orderId){

        String merchant = "testqnbaatest001";

        Double amount =20d;

        String checkoutType = "Checkout.showLightbox()";

        StringBuilder paymentHtml = new StringBuilder();
        paymentHtml.append("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Payment</title>\n" +
                "    <script src=\"//netdna.bootstrapcdn.com/bootstrap/3.0.0/js/bootstrap.min.js\"></script>\n" +
                "    <link href=\"//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css\" rel=\"stylesheet\" id=\"bootstrap-css\">\n" +
                "    <script src=\"//netdna.bootstrapcdn.com/bootstrap/3.0.0/js/bootstrap.min.js\"></script>\n" +
                "    <script src=\"//code.jquery.com/jquery-1.11.1.min.js\"></script>\n" +
                "    <script src=\"https://qnbalahli.test.gateway.mastercard.com/checkout/version/51/checkout.js\"\n" +
                "            data-error=\"errorCallback\"\n" +
                "            data-cancel=\"cancelCallback\">\n" +
                "    </script>\n" +
                "\n" +
                "    <script type=\"text/javascript\">\n" +
                "            function errorCallback(error) {\n" +
                "                  console.log(JSON.stringify(error));\n" +
                "            }\n" +
                "            function cancelCallback() {\n" +
                "                  console.log('Payment cancelled');\n" +
                "            }\n" +
                "\n" +
                "            Checkout.configure({                merchant: '");

        paymentHtml.append(merchant+"',\n" +
                "                order: {\n" +
                "                    amount: function() {");

        paymentHtml.append(amount+"},\n" +
                "                    currency: 'EGP',\n" +
                "                    description: 'Ordered goods',\n" +
                "                   id: '");

        paymentHtml.append(orderId+"'\n" +
                "                },\n" +
                "                interaction: {\n" +
                "                    merchant: {\n" +
                "                        name: 'Your merchant name',\n" +
                "                        address: {\n" +
                "                            line1: '200 Sample St',\n" +
                "                            line2: '1234 Example Town'\n" +
                "                        }\n" +
                "                    }\n" +
                "                                                                }\n" +
                "            });\n" +
                "        </script>\n" +
                "    <script src=\"//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/js/bootstrap.min.js\"></script>\n" +
                "    <script src=\"//code.jquery.com/jquery-1.11.1.min.js\"></script>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"container\">\n" +
                "    <div class=\"row\">\n" +
                "        <div class=\"span12\">\n" +
                "            <form class=\"form-horizontal span6\">\n" +
                "                <fieldset>\n" +
                "                    <legend>Payment</legend>\n" +
                "\n" +
                "                    <div class=\"control-group\">\n" +
                "                        <label class=\"control-label\">Card Holder's Name</label>\n" +
                "                        <div class=\"controls\">\n" +
                "                            <input type=\"text\" class=\"input-block-level\" pattern=\"\\w+ \\w+.*\" title=\"Fill your first and last name\" required>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "\n" +
                "                    <div class=\"control-group\">\n" +
                "                        <label class=\"control-label\">Card Number</label>\n" +
                "                        <div class=\"controls\">\n" +
                "                            <div class=\"row-fluid\">\n" +
                "                                <div class=\"span3\">\n" +
                "                                    <input type=\"text\" class=\"input-block-level\" autocomplete=\"off\" minlength=\"16\" maxlength=\"16\" pattern=\"\\d{16}\" title=\"Card number of 16 digits\" required>\n" +
                "                                </div>\n" +
                "                            </div>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "\n" +
                "                    <div class=\"control-group\">\n" +
                "                        <label class=\"control-label\">Card Expiry Date</label>\n" +
                "                        <div class=\"controls\">\n" +
                "                            <div class=\"row-fluid\">\n" +
                "                                <div class=\"span9\">\n" +
                "                                    <select class=\"input-block-level\">\n" +
                "                                        <option>January</option>\n" +
                "                                        <option>February</option>\n" +
                "                                        <option>March</option>\n" +
                "                                        <option>April</option>\n" +
                "                                        <option>May</option>\n" +
                "                                        <option>June</option>\n" +
                "                                        <option>July</option>\n" +
                "                                        <option>August</option>\n" +
                "                                        <option>September</option>\n" +
                "                                        <option>October</option>\n" +
                "                                        <option>November</option>\n" +
                "                                        <option>December</option>\n" +
                "                                    </select>\n" +
                "                                </div>\n" +
                "                                <div class=\"span3\">\n" +
                "                                    <select class=\"input-block-level\">\n" +
                "                                        <option>2019</option>\n" +
                "                                        <option>2020</option>\n" +
                "                                        <option>2021</option>\n" +
                "                                        <option>2022</option>\n" +
                "                                        <option>2023</option>\n" +
                "                                        <option>2024</option>\n" +
                "                                        <option>2025</option>\n" +
                "                                        <option>2026</option>\n" +
                "                                        <option>2027</option>\n" +
                "                                        <option>2028</option>\n" +
                "                                        <option>2029</option>\n" +
                "                                    </select>\n" +
                "                                </div>\n" +
                "                            </div>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "\n" +
                "                    <div class=\"control-group\">\n" +
                "                        <label class=\"control-label\">Card CVV</label>\n" +
                "                        <div class=\"controls\">\n" +
                "                            <div class=\"row-fluid\">\n" +
                "                                <div class=\"span3\">\n" +
                "                                    <input type=\"text\" class=\"input-block-level\" autocomplete=\"off\" maxlength=\"3\" pattern=\"\\d{3}\" title=\"Three digits at back of your card\" required>\n" +
                "                                </div>\n" +
                "                                <div class=\"span8\">\n" +
                "                                    <!-- screenshot may be here -->\n" +
                "                                </div>\n" +
                "                            </div>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "\n" +
                "                    <div class=\"form-actions\">\n" +
                "                        <button type=\"submit\" class=\"btn btn-primary\" onclick=\"");

        paymentHtml.append(checkoutType);

        paymentHtml.append("\">Submit</button>\n" +
                "                        <button type=\"button\" class=\"btn\">Cancel</button>\n" +
                "                    </div>\n" +
                "                </fieldset>\n" +
                "            </form>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>");

        return new ResponseEntity<>(paymentHtml.toString(), HttpStatus.OK);

    }

}
