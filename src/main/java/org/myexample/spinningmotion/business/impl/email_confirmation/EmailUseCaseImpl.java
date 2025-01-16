package org.myexample.spinningmotion.business.impl.email_confirmation;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myexample.spinningmotion.business.exception.EmailSendingException;
import org.myexample.spinningmotion.business.interfc.EmailUseCase;
import org.myexample.spinningmotion.business.interfc.RecordImageUseCase;
import org.myexample.spinningmotion.domain.stripe.CheckoutRequest;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.myexample.spinningmotion.persistence.entity.RecordImageEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


import java.util.Base64;
import java.util.List;

import static org.myexample.spinningmotion.business.impl.email_confirmation.EmailUseCaseImpl.HtmlTags.*;

@Service
@Slf4j
@Profile("!test")
@RequiredArgsConstructor
public class EmailUseCaseImpl implements EmailUseCase {
    public static class HtmlTags{
        private HtmlTags() {
            throw new IllegalStateException("Utility class");
        }
        public static final String DIV_CLOSE = "</div>";
        public static final String DIV_OPEN = "<div>";
        public static final String DIV_CLASS_OPEN = "<div class='%s'>";
        public static final String CONTENT_DIV = "<div class='content'>";
        public static final String CONTAINER_DIV = "<div class='container'>";
        public static final String HEADER_DIV = "<div class='header'>";
        public static final String ITEM_DIV = "<div class='item'>";
        public static final String ITEM_CONTENT_DIV = "<div class='item-content'>";
        public static final String ITEM_HEADER_DIV = "<div class='item-header'>";
        public static final String ITEM_DETAILS_DIV = "<div class='item-details'>";
        // CSS Class name constants
        public static final String CSS_ITEM_PRICE = "item-price";
        public static final String CSS_DETAIL_LABEL = "detail-label";
        public static final String CSS_ITEM_TITLE = "item-title";
        public static final String CSS_ITEM_ARTIST = "item-artist";
        public static final String CSS_ITEM_QUANTITY = "item-quantity";
        public static final String CSS_FOOTER = "footer";
        public static final String CSS_TOTAL = "total";
        public static final String CSS_ORDER_NUMBER = "order-number";
        public static final String HTML_HEAD = """
                <!DOCTYPE html>
                <html>
                <head>
                <meta charset='UTF-8'>
                """;
        public static final String COMMON_STYLES = """
            <style>
            body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f3f4f6; }
            .container { max-width: 600px; margin: 32px auto; background: #ffffff; }
            .header { background-color: #dc2626; padding: 32px; color: white; text-align: center; border-bottom: 8px solid #000000; }
            .content { padding: 32px; }
            .order-number { display: inline-block; background: #fbbf24; padding: 8px 16px; color: #000000; font-weight: bold; border: 4px solid #000000; }
            .item { margin-bottom: 24px; border: 4px solid #000000; display: flex; }
            .item-image { width: 120px; height: 120px; background: #3b82f6; border-right: 4px solid #000000; flex-shrink: 0; background-position: center; background-size: cover; }
            .item-content { flex-grow: 1; display: flex; flex-direction: column; }
            .item-header { background: #000000; color: white; padding: 12px; font-weight: bold; }
            .item-details { padding: 16px; display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; }
            .item-title { font-size: 18px; font-weight: bold; }
            .item-artist { color: #666666; }
            .item-price { background: #fbbf24; padding: 4px 12px; font-weight: bold; text-align: right; }
            .item-quantity { display: flex; align-items: center; justify-content: center; border: 2px solid #000000; }
            .quantity-button { background: #000000; color: white; border: none; width: 32px; height: 32px; font-size: 18px; cursor: pointer; }
            .quantity-value { padding: 0 16px; font-weight: bold; }
            .total { background: #fbbf24; padding: 24px; text-align: right; font-weight: bold; border: 4px solid #000000; }
            .footer { padding: 24px; text-align: center; color: #666666; }
            </style>
            """;
    }
    private final JavaMailSender mailSender;
    private final RecordImageUseCase recordImageUseCase;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendOrderConfirmation(String to, List<CheckoutRequest.Item> items,
                                      double totalAmount, String orderNumber) {
        try {
            log.debug("Starting emailtest send process");
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
            log.error("Failed to send emailtest", e);
            throw new EmailSendingException("Failed to send order confirmation emailtest", e.getMessage());
        }
    }
    @Override
    public void sendNewReleaseNotification(String to, List<RecordEntity> newRecords) {
        try {
            log.debug("Starting new release notification emailtest send process");
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String htmlContent = generateNewReleaseEmailContent(newRecords);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("New Release Alert: " + newRecords.size() + " New Records Added!");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.debug("New release notification emailtest sent successfully");
        } catch (Exception e) {
            log.error("Failed to send new release notification", e);
            throw new EmailSendingException("Failed to send new release notification", e.getMessage());
        }
    }
    private String convertImageToBase64(byte[] imageData, String imageType) {
        return "data:" + imageType + ";base64," + Base64.getEncoder().encodeToString(imageData);
    }
    private String generateEmailContent(List<CheckoutRequest.Item> items, double totalAmount, String orderNumber) {
        StringBuilder html = new StringBuilder();
        html.append(HTML_HEAD)
                .append(COMMON_STYLES)
                .append("</head>")
                .append("<body>")
                .append(CONTAINER_DIV)
                .append(HEADER_DIV)
                .append("<h1 style='margin: 0;'>Order Confirmation</h1>")
                .append(String.format(DIV_CLASS_OPEN, CSS_ORDER_NUMBER)).append("Order #").append(orderNumber).append(DIV_CLOSE)
                .append(DIV_CLOSE)
                .append(CONTENT_DIV);

        // Items
        for (CheckoutRequest.Item item : items) {
            List<RecordImageEntity> images = recordImageUseCase.getImagesByRecordId(item.getRecordId());
            String imageHtml = "<div class='item-image'></div>";

            if (!images.isEmpty()) {
                RecordImageEntity firstImage = images.get(0);
                String base64Image = convertImageToBase64(firstImage.getImageData(), firstImage.getImageType());
                imageHtml = "<div class='item-image' style='background-image: url(" + base64Image + "); background-size: cover; background-position: center;'></div>";
            }
            double displayPrice = item.getDiscountedPrice() != null ? item.getDiscountedPrice() : item.getPrice();
            double itemTotal = displayPrice * item.getQuantity();

            html.append(ITEM_DIV)
                    .append(imageHtml)
                    .append(ITEM_CONTENT_DIV)
                    .append(ITEM_HEADER_DIV)
                    .append(String.format(DIV_CLASS_OPEN, CSS_ITEM_TITLE)).append(item.getTitle()).append(DIV_CLOSE)
                    .append(String.format(DIV_CLASS_OPEN, CSS_ITEM_ARTIST)).append(item.getArtist()).append(DIV_CLOSE)
                    .append(DIV_CLOSE)
                    .append(ITEM_DETAILS_DIV)
                    .append(DIV_OPEN).append("Condition: ").append(item.getCondition()).append(DIV_CLOSE)
                    .append(String.format(DIV_CLASS_OPEN, CSS_ITEM_PRICE)).append("€").append(String.format("%.2f", displayPrice)).append(DIV_CLOSE)
                    .append(String.format(DIV_CLASS_OPEN, CSS_ITEM_QUANTITY))
                    .append("<span class='quantity-value'>Quantity: ").append(item.getQuantity()).append("</span>")
                    .append(DIV_CLOSE)
                    .append(String.format(DIV_CLASS_OPEN, CSS_ITEM_PRICE)).append("Total: €").append(String.format("%.2f", itemTotal)).append(DIV_CLOSE)
                    .append(DIV_CLOSE)
                    .append(DIV_CLOSE)
                    .append(DIV_CLOSE);
        }

        // Total
        html.append(DIV_CLOSE)
                .append(String.format(DIV_CLASS_OPEN, CSS_TOTAL))
                .append("Total Amount: €").append(String.format("%.2f", totalAmount))
                .append(DIV_CLOSE)
                .append(String.format(DIV_CLASS_OPEN, CSS_FOOTER))
                .append("<p>Thank you for shopping at SpinningMotion!</p>")
                .append("<p>If you have any questions, please contact our support team.</p>")
                .append(DIV_CLOSE)
                .append(DIV_CLOSE)
                .append("</body></html>");

        return html.toString();
    }
    private String generateNewReleaseEmailContent(List<RecordEntity> newRecords) {
        StringBuilder html = new StringBuilder();
        html.append(HtmlTags.HTML_HEAD)
                .append(HtmlTags.COMMON_STYLES)
                .append("</head>")
                .append("<body>")
                .append(HtmlTags.CONTAINER_DIV)
                .append(HtmlTags.HEADER_DIV)
                .append("<h1 style='margin: 0;'>New Releases Alert!</h1>")
                .append("<p style='margin-top: 16px;'>").append(newRecords.size()).append(" New ")
                .append(newRecords.size() > 1 ? "Records" : "Record").append(" Added</p>")
                .append(DIV_CLOSE)
                .append(HtmlTags.CONTENT_DIV);

        // Process each record using the same item card layout as order confirmation
        // Process each record using the same item card layout as order confirmation
        for (RecordEntity vinylRecord : newRecords) {
            List<RecordImageEntity> images = recordImageUseCase.getImagesByRecordId(vinylRecord.getId());
            String imageHtml = "<div class='item-image'></div>";

            if (!images.isEmpty()) {
                RecordImageEntity firstImage = images.get(0);
                String base64Image = convertImageToBase64(firstImage.getImageData(), firstImage.getImageType());
                imageHtml = "<div class='item-image' style='background-image: url(" + base64Image + "); background-size: cover; background-position: center;'></div>";
            }

            html.append(String.format(DIV_CLASS_OPEN, "item"))
                    .append(imageHtml)
                    .append(String.format(DIV_CLASS_OPEN, "item-content"))
                    .append(String.format(DIV_CLASS_OPEN, "item-header"))
                    .append(String.format(DIV_CLASS_OPEN, HtmlTags.CSS_ITEM_TITLE)).append(vinylRecord.getTitle()).append(DIV_CLOSE)
                    .append(String.format(DIV_CLASS_OPEN, HtmlTags.CSS_ITEM_ARTIST)).append(vinylRecord.getArtist()).append(DIV_CLOSE)
                    .append(DIV_CLOSE)
                    .append(String.format(DIV_CLASS_OPEN, "item-details"))
                    .append(DIV_OPEN)
                    .append(String.format(DIV_CLASS_OPEN, HtmlTags.CSS_DETAIL_LABEL)).append("Genre").append(DIV_CLOSE)
                    .append(DIV_OPEN).append(vinylRecord.getGenre().getName()).append(DIV_CLOSE)
                    .append(DIV_CLOSE)
                    .append(DIV_OPEN)
                    .append(String.format(DIV_CLASS_OPEN, HtmlTags.CSS_DETAIL_LABEL)).append("Price").append(DIV_CLOSE)
                    .append(String.format(DIV_CLASS_OPEN, HtmlTags.CSS_ITEM_PRICE)).append("€")
                    .append(String.format("%.2f", vinylRecord.getPrice())).append(DIV_CLOSE)
                    .append(DIV_CLOSE)
                    .append(DIV_OPEN)
                    .append(String.format(DIV_CLASS_OPEN, HtmlTags.CSS_DETAIL_LABEL)).append("Release Year").append(DIV_CLOSE)
                    .append(DIV_OPEN).append(vinylRecord.getYear()).append(DIV_CLOSE)
                    .append(DIV_CLOSE)
                    .append(DIV_OPEN)
                    .append(String.format(DIV_CLASS_OPEN, CSS_DETAIL_LABEL)).append("Condition").append(DIV_CLOSE)
                    .append(DIV_OPEN).append(vinylRecord.getCondition()).append(DIV_CLOSE)
                    .append(DIV_CLOSE)
                    .append(DIV_CLOSE)
                    .append(DIV_CLOSE)
                    .append(DIV_CLOSE);
        }


         html.append(HtmlTags.DIV_CLOSE)
            .append(String.format(DIV_CLASS_OPEN, HtmlTags.CSS_FOOTER))
            .append("<p>Visit SpinningMotion to check out these new releases!</p>")
            .append("<p>You received this emailtest because you subscribed to new release notifications.</p>")
            .append(HtmlTags.DIV_CLOSE)
            .append(HtmlTags.DIV_CLOSE)
            .append("</body></html>");

        return html.toString();
    }
}