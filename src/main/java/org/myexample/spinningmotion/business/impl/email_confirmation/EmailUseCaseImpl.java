package org.myexample.spinningmotion.business.impl.email_confirmation;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myexample.spinningmotion.business.exception.EmailSendingException;
import org.myexample.spinningmotion.business.interfc.EmailUseCase;
import org.myexample.spinningmotion.domain.stripe.CheckoutRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@Slf4j
@Profile("!test")
@RequiredArgsConstructor
public class EmailUseCaseImpl implements EmailUseCase {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendOrderConfirmation(String to, List<CheckoutRequest.Item> items,
                                      double totalAmount, String orderNumber) {
        try {
            log.debug("Starting email send process");
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String htmlContent = generateEmailContent(items, totalAmount, orderNumber);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Order Confirmation #" + orderNumber);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.debug("Email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send email", e);
            throw new EmailSendingException("Failed to send order confirmation email", e.getMessage());
        }
    }

    private String generateEmailContent(List<CheckoutRequest.Item> items, double totalAmount, String orderNumber) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<meta charset='UTF-8'>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f3f4f6; }")
                .append(".container { max-width: 600px; margin: 32px auto; background: #ffffff; }")
                .append(".header { background-color: #dc2626; padding: 32px; color: white; text-align: center; border-bottom: 8px solid #000000; }")
                .append(".content { padding: 32px; }")
                .append(".order-number { display: inline-block; background: #fbbf24; padding: 8px 16px; color: #000000; font-weight: bold; border: 4px solid #000000; }")
                .append(".item { margin-bottom: 24px; border: 4px solid #000000; display: flex; }")
                .append(".item-image { width: 120px; height: 120px; background: #3b82f6; border-right: 4px solid #000000; flex-shrink: 0; }")
                .append(".item-content { flex-grow: 1; display: flex; flex-direction: column; }")
                .append(".item-header { background: #000000; color: white; padding: 12px; font-weight: bold; }")
                .append(".item-details { padding: 16px; display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; }")
                .append(".item-title { font-size: 18px; font-weight: bold; }")
                .append(".item-artist { color: #666666; }")
                .append(".item-price { background: #fbbf24; padding: 4px 12px; font-weight: bold; text-align: right; }")
                .append(".item-quantity { display: flex; align-items: center; justify-content: center; border: 2px solid #000000; }")
                .append(".quantity-button { background: #000000; color: white; border: none; width: 32px; height: 32px; font-size: 18px; cursor: pointer; }")
                .append(".quantity-value { padding: 0 16px; font-weight: bold; }")
                .append(".total { background: #fbbf24; padding: 24px; text-align: right; font-weight: bold; border: 4px solid #000000; }")
                .append(".footer { padding: 24px; text-align: center; color: #666666; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class='container'>")
                .append("<div class='header'>")
                .append("<h1 style='margin: 0;'>Order Confirmation</h1>")
                .append("<div class='order-number'>Order #").append(orderNumber).append("</div>")
                .append("</div>")
                .append("<div class='content'>");

        // Items
        for (CheckoutRequest.Item item : items) {
            html.append("<div class='item'>")
                    .append("<div class='item-image'></div>")
                    .append("<div class='item-content'>")
                    .append("<div class='item-header'>")
                    .append("<div class='item-title'>").append(item.getTitle()).append("</div>")
                    .append("<div class='item-artist'>").append(item.getArtist()).append("</div>")
                    .append("</div>")
                    .append("<div class='item-details'>")
                    .append("<div>Condition: ").append(item.getCondition()).append("</div>")
                    .append("<div class='item-price'>€").append(String.format("%.2f", item.getPrice())).append("</div>")
                    .append("<div class='item-quantity'>")
                    .append("<span class='quantity-value'>Quantity: ").append(item.getQuantity()).append("</span>")
                    .append("</div>")
                    .append("<div class='item-price'>Total: €").append(String.format("%.2f", item.getPrice() * item.getQuantity())).append("</div>")
                    .append("</div>")
                    .append("</div>")
                    .append("</div>");
        }

        // Total
        html.append("</div>")
                .append("<div class='total'>")
                .append("Total Amount: €").append(String.format("%.2f", totalAmount))
                .append("</div>")
                .append("<div class='footer'>")
                .append("<p>Thank you for shopping at SpinningMotion!</p>")
                .append("<p>If you have any questions, please contact our support team.</p>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");

        return html.toString();
    }
}