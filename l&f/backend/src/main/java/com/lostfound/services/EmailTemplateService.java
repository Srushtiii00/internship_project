package com.lostfound.services;

import com.lostfound.entities.Claim;
import com.lostfound.entities.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailTemplateService {

    @Value("${app.mail.portal-name:Lost & Found Portal}")
    private String portalName;

    @Value("${app.mail.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Shared responsive wrapper for all email templates
     */
    private String wrapWithLayout(String preheader, String headerTitle, String contentHtml, String ctaText, String ctaUrl) {
        String ctaButton = "";
        if (ctaText != null && ctaUrl != null) {
            ctaButton = """
                <div style="text-align: center; margin: 30px 0 10px;">
                    <a href="%s" style="background-color: #4f46e5; color: #ffffff; padding: 12px 28px; text-decoration: none; border-radius: 6px; font-weight: 600; display: inline-block; font-size: 15px;">
                        %s
                    </a>
                </div>
            """.formatted(ctaUrl, ctaText);
        }

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
            </head>
            <body style="margin: 0; padding: 0; background-color: #f1f5f9; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #334155;">
                <div style="display: none; max-height: 0px; overflow: hidden;">%s</div>
                <table width="100%%" border="0" cellspacing="0" cellpadding="0" style="background-color: #f1f5f9; padding: 30px 15px;">
                    <tr>
                        <td align="center">
                            <table width="100%%" max-width="600" border="0" cellspacing="0" cellpadding="0" style="max-width: 600px; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -2px rgba(0, 0, 0, 0.05);">
                                <!-- Brand Header -->
                                <tr>
                                    <td style="background-color: #4f46e5; padding: 24px 32px; text-align: left;">
                                        <div style="color: #ffffff; font-size: 20px; font-weight: 700; letter-spacing: -0.5px;">
                                            🔍 %s
                                        </div>
                                        <div style="color: #e0e7ff; font-size: 13px; margin-top: 4px;">
                                            Community Recovery & Ownership Verification Network
                                        </div>
                                    </td>
                                </tr>
                                <!-- Main Content Area -->
                                <tr>
                                    <td style="padding: 32px;">
                                        <h2 style="margin-top: 0; color: #0f172a; font-size: 20px; font-weight: 700; margin-bottom: 16px;">
                                            %s
                                        </h2>
                                        %s
                                        %s
                                    </td>
                                </tr>
                                <!-- Footer -->
                                <tr>
                                    <td style="background-color: #f8fafc; padding: 20px 32px; border-top: 1px solid #e2e8f0; text-align: center; font-size: 12px; color: #64748b; line-height: 1.5;">
                                        <p style="margin: 0 0 8px;">This is an automated transactional notification from %s.</p>
                                        <p style="margin: 0;">Please do not reply directly to this email. For assistance or questions, visit our portal.</p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
        """.formatted(headerTitle, preheader, portalName, headerTitle, contentHtml, ctaButton, portalName);
    }

    public String buildRegistrationWelcomeHtml(String fullName, String username) {
        String content = """
            <p style="font-size: 15px; line-height: 1.6; margin-bottom: 20px;">
                Hello <strong>%s</strong>,
            </p>
            <p style="font-size: 15px; line-height: 1.6; margin-bottom: 20px;">
                Welcome to <strong>%s</strong>! Your user account has been successfully created. You can now report lost or found belongings, search active listings, and verify ownership claims.
            </p>
            <div style="background-color: #f8fafc; border-left: 4px solid #4f46e5; padding: 16px; border-radius: 4px; margin-bottom: 24px;">
                <p style="margin: 0 0 8px; font-size: 14px;"><strong>Your Username:</strong> %s</p>
                <p style="margin: 0; font-size: 14px; color: #64748b;">Keep your credentials secure. We will never ask for your password via email.</p>
            </div>
            <p style="font-size: 15px; line-height: 1.6;">
                Click the button below to sign in and explore the portal.
            </p>
        """.formatted(escape(fullName), portalName, escape(username));

        return wrapWithLayout(
                "Welcome to " + portalName,
                "Welcome to " + portalName,
                content,
                "Sign In to Your Account",
                frontendUrl + "/login"
        );
    }

    public String buildItemReportedHtml(String fullName, Item item) {
        String itemTypeBadge = item.getType().name().equals("LOST")
                ? "<span style=\"background-color: #fee2e2; color: #b91c1c; padding: 4px 10px; border-radius: 20px; font-weight: 600; font-size: 12px;\">LOST ITEM</span>"
                : "<span style=\"background-color: #dbeafe; color: #1d4ed8; padding: 4px 10px; border-radius: 20px; font-weight: 600; font-size: 12px;\">FOUND ITEM</span>";

        String content = """
            <p style="font-size: 15px; line-height: 1.6; margin-bottom: 20px;">
                Hello <strong>%s</strong>,
            </p>
            <p style="font-size: 15px; line-height: 1.6; margin-bottom: 20px;">
                Your report has been successfully submitted and is now published to the portal. Community members can search and view this listing.
            </p>
            <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 20px; margin-bottom: 24px;">
                <div style="margin-bottom: 12px;">%s</div>
                <h3 style="margin: 0 0 12px; font-size: 17px; color: #0f172a;">%s</h3>
                <p style="margin: 0 0 8px; font-size: 14px; color: #475569;"><strong>Category:</strong> %s</p>
                <p style="margin: 0 0 8px; font-size: 14px; color: #475569;"><strong>Location:</strong> %s</p>
                <p style="margin: 0 0 8px; font-size: 14px; color: #475569;"><strong>Date:</strong> %s</p>
                <p style="margin: 0; font-size: 14px; color: #475569;"><strong>Status:</strong> %s</p>
            </div>
            <p style="font-size: 15px; line-height: 1.6;">
                You will receive an email update whenever someone submits a claim or there are updates regarding this listing.
            </p>
        """.formatted(
                escape(fullName),
                itemTypeBadge,
                escape(item.getTitle()),
                escape(item.getCategory()),
                escape(item.getLocation()),
                item.getItemDate() != null ? item.getItemDate().toString() : "N/A",
                item.getStatus().name()
        );

        return wrapWithLayout(
                "Your listing has been published: " + item.getTitle(),
                "Listing Report Confirmed",
                content,
                "View Your Listing",
                frontendUrl + "/items/" + item.getId()
        );
    }

    public String buildNewClaimReceivedHtml(String ownerName, Item item, Claim claim) {
        String content = """
            <p style="font-size: 15px; line-height: 1.6; margin-bottom: 20px;">
                Hello <strong>%s</strong>,
            </p>
            <p style="font-size: 15px; line-height: 1.6; margin-bottom: 20px;">
                A community member has submitted an ownership claim for your listing: <strong>%s</strong>.
            </p>
            <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 20px; margin-bottom: 24px;">
                <h4 style="margin: 0 0 12px; font-size: 15px; color: #0f172a;">Claim Details</h4>
                <p style="margin: 0 0 8px; font-size: 14px; color: #475569;"><strong>Claimant:</strong> %s</p>
                <p style="margin: 0 0 8px; font-size: 14px; color: #475569;"><strong>Submitted Contact:</strong> %s</p>
                <div style="margin-top: 12px; padding: 12px; background: #ffffff; border-radius: 6px; border: 1px solid #cbd5e1;">
                    <span style="font-size: 13px; font-weight: 600; color: #334155; display: block; margin-bottom: 4px;">Ownership Proof Provided:</span>
                    <span style="font-size: 14px; color: #475569; font-style: italic;">"%s"</span>
                </div>
            </div>
            <p style="font-size: 15px; line-height: 1.6;">
                Please review the proof details and decide whether to approve or reject this claim from your User Dashboard.
            </p>
        """.formatted(
                escape(ownerName),
                escape(item.getTitle()),
                escape(claim.getClaimant() != null ? claim.getClaimant().getFullName() : "A user"),
                escape(claim.getContactInfo()),
                escape(claim.getProofDescription())
        );

        return wrapWithLayout(
                "New claim received for " + item.getTitle(),
                "New Ownership Claim Received",
                content,
                "Review Claim on Dashboard",
                frontendUrl + "/items/" + item.getId()
        );
    }

    public String buildClaimApprovedHtml(String claimantName, Item item, String remarks) {
        String content = """
            <p style="font-size: 15px; line-height: 1.6; margin-bottom: 20px;">
                Hello <strong>%s</strong>,
            </p>
            <div style="background-color: #ecfdf5; border-left: 4px solid #10b981; padding: 16px; border-radius: 4px; margin-bottom: 24px;">
                <p style="margin: 0; font-size: 15px; color: #065f46; font-weight: 600;">
                    🎉 Good news! Your ownership claim has been verified and APPROVED.
                </p>
            </div>
            <p style="font-size: 15px; line-height: 1.6; margin-bottom: 20px;">
                The reporter for <strong>%s</strong> has accepted your proof of ownership.
            </p>
            <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 20px; margin-bottom: 24px;">
                <p style="margin: 0 0 8px; font-size: 14px; color: #475569;"><strong>Item:</strong> %s</p>
                <p style="margin: 0 0 8px; font-size: 14px; color: #475569;"><strong>Category:</strong> %s</p>
                <p style="margin: 0 0 8px; font-size: 14px; color: #475569;"><strong>Location:</strong> %s</p>
                <div style="margin-top: 12px; padding: 12px; background: #ffffff; border-radius: 6px; border: 1px solid #cbd5e1;">
                    <span style="font-size: 13px; font-weight: 600; color: #334155; display: block; margin-bottom: 4px;">Reporter's Handover / Contact Notes:</span>
                    <span style="font-size: 14px; color: #047857; font-weight: 500;">%s</span>
                </div>
            </div>
            <p style="font-size: 15px; line-height: 1.6;">
                Please coordinate with the reporter to collect your item safely.
            </p>
        """.formatted(
                escape(claimantName),
                escape(item.getTitle()),
                escape(item.getTitle()),
                escape(item.getCategory()),
                escape(item.getLocation()),
                (remarks != null && !remarks.trim().isEmpty()) ? escape(remarks) : "No specific notes provided. Check the portal for details."
        );

        return wrapWithLayout(
                "Your claim for " + item.getTitle() + " was APPROVED!",
                "Claim Approved",
                content,
                "View Item & Claim",
                frontendUrl + "/dashboard"
        );
    }

    public String buildClaimRejectedHtml(String claimantName, Item item, String remarks) {
        String content = """
            <p style="font-size: 15px; line-height: 1.6; margin-bottom: 20px;">
                Hello <strong>%s</strong>,
            </p>
            <p style="font-size: 15px; line-height: 1.6; margin-bottom: 20px;">
                Your claim for the item <strong>%s</strong> was reviewed by the reporter, but could not be verified and has been declined.
            </p>
            <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 20px; margin-bottom: 24px;">
                <p style="margin: 0 0 8px; font-size: 14px; color: #475569;"><strong>Item:</strong> %s</p>
                <div style="margin-top: 12px; padding: 12px; background: #ffffff; border-radius: 6px; border: 1px solid #cbd5e1;">
                    <span style="font-size: 13px; font-weight: 600; color: #334155; display: block; margin-bottom: 4px;">Decision Remarks:</span>
                    <span style="font-size: 14px; color: #b91c1c;">%s</span>
                </div>
            </div>
            <p style="font-size: 15px; line-height: 1.6;">
                If you believe this was in error, please browse active listings or submit a detailed lost item report with identifying features.
            </p>
        """.formatted(
                escape(claimantName),
                escape(item.getTitle()),
                escape(item.getTitle()),
                (remarks != null && !remarks.trim().isEmpty()) ? escape(remarks) : "The proof provided did not match the item's verified details."
        );

        return wrapWithLayout(
                "Update regarding your claim for " + item.getTitle(),
                "Claim Status Update",
                content,
                "Browse Lost & Found Listings",
                frontendUrl + "/browse"
        );
    }

    public String buildItemRecoveredHtml(String recipientName, Item item) {
        String content = """
            <p style="font-size: 15px; line-height: 1.6; margin-bottom: 20px;">
                Hello <strong>%s</strong>,
            </p>
            <div style="background-color: #ecfdf5; border-left: 4px solid #10b981; padding: 16px; border-radius: 4px; margin-bottom: 24px;">
                <p style="margin: 0; font-size: 15px; color: #065f46; font-weight: 600;">
                    ✨ Item Successfully Recovered & Closed!
                </p>
            </div>
            <p style="font-size: 15px; line-height: 1.6; margin-bottom: 20px;">
                The listing for <strong>%s</strong> has been officially marked as <strong>RECOVERED</strong>. Thank you for contributing to the community!
            </p>
            <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 20px; margin-bottom: 24px;">
                <p style="margin: 0 0 8px; font-size: 14px; color: #475569;"><strong>Item:</strong> %s</p>
                <p style="margin: 0 0 8px; font-size: 14px; color: #475569;"><strong>Category:</strong> %s</p>
                <p style="margin: 0; font-size: 14px; color: #475569;"><strong>Status:</strong> RECOVERED</p>
            </div>
        """.formatted(
                escape(recipientName),
                escape(item.getTitle()),
                escape(item.getTitle()),
                escape(item.getCategory())
        );

        return wrapWithLayout(
                "Item Recovered: " + item.getTitle(),
                "Item Successfully Recovered",
                content,
                "View Your Dashboard",
                frontendUrl + "/dashboard"
        );
    }

    public String buildPotentialMatchHtml(String recipientName, Item originalItem, List<Item> matches) {
        StringBuilder itemsListHtml = new StringBuilder();
        for (Item match : matches) {
            itemsListHtml.append("""
                <div style="padding: 12px; margin-bottom: 10px; background: #ffffff; border-radius: 6px; border: 1px solid #cbd5e1;">
                    <div style="font-size: 15px; font-weight: 600; color: #0f172a; margin-bottom: 4px;">
                        %s
                    </div>
                    <div style="font-size: 13px; color: #64748b; margin-bottom: 8px;">
                        Reported on %s at %s
                    </div>
                    <a href="%s/items/%d" style="font-size: 13px; color: #4f46e5; text-decoration: none; font-weight: 600;">
                        View Match Details &rarr;
                    </a>
                </div>
            """.formatted(
                    escape(match.getTitle()),
                    match.getItemDate() != null ? match.getItemDate().toString() : "Recent",
                    escape(match.getLocation()),
                    frontendUrl,
                    match.getId()
            ));
        }

        String content = """
            <p style="font-size: 15px; line-height: 1.6; margin-bottom: 20px;">
                Hello <strong>%s</strong>,
            </p>
            <div style="background-color: #eef2ff; border-left: 4px solid #4f46e5; padding: 16px; border-radius: 4px; margin-bottom: 24px;">
                <p style="margin: 0; font-size: 15px; color: #3730a3; font-weight: 600;">
                    🔍 Potential Item Match Detected!
                </p>
            </div>
            <p style="font-size: 15px; line-height: 1.6; margin-bottom: 20px;">
                We found potential matching items that might correspond to your report: <strong>%s</strong> (%s).
            </p>
            <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 20px; margin-bottom: 24px;">
                <h4 style="margin: 0 0 12px; font-size: 15px; color: #0f172a;">Potential Matching Listings (%d):</h4>
                %s
            </div>
            <p style="font-size: 15px; line-height: 1.6;">
                Click any of the match links above to check if it's your item and submit a claim if applicable.
            </p>
        """.formatted(
                escape(recipientName),
                escape(originalItem.getTitle()),
                originalItem.getType().name(),
                matches.size(),
                itemsListHtml.toString()
        );

        return wrapWithLayout(
                "Potential match found for " + originalItem.getTitle(),
                "Potential Match Notification",
                content,
                "Explore All Listings",
                frontendUrl + "/browse"
        );
    }

    private String escape(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
