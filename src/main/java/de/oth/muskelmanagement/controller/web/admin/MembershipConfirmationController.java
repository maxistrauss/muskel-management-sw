package de.oth.muskelmanagement.controller.web.admin;

import de.oth.muskelmanagement.model.entity.MembershipConfirmation;
import de.oth.muskelmanagement.service.PdfGenerationService;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/membership-confirmations")
public class MembershipConfirmationController {

    private final PdfGenerationService pdfService;

    public MembershipConfirmationController(PdfGenerationService pdfService) {
        this.pdfService = pdfService;
    }

    /**
     * List all membership confirmations
     */
    @GetMapping
    public String listConfirmations(Model model, @RequestParam(required = false) Long userId) {
        List<MembershipConfirmation> confirmations;

        if (userId != null) {
            confirmations = pdfService.getConfirmationsByUser(userId);
            model.addAttribute("filterUserId", userId);
        } else {
            confirmations = pdfService.getAllConfirmations();
        }

        model.addAttribute("confirmations", confirmations);
        return "admin/membership-confirmations";
    }

    /**
     * Generate a new membership confirmation PDF
     */
    @PostMapping("/generate")
    public String generateConfirmation(@RequestParam Long userId, @RequestParam Long subscriptionId,
            Principal principal, RedirectAttributes redirectAttributes) {
        try {
            MembershipConfirmation confirmation = pdfService.generateMembershipConfirmation(userId, subscriptionId,
                    principal.getName());

            redirectAttributes.addFlashAttribute("success",
                    "Membership confirmation generated successfully (ID: " + confirmation.getId() + ")");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", "Cannot generate confirmation: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Failed to generate membership confirmation: " + e.getMessage());
        }

        return "redirect:/admin/users/edit/" + userId;
    }

    /**
     * Preview a PDF in the browser
     */
    @GetMapping("/preview/{id}")
    public ResponseEntity<byte[]> previewPdf(@PathVariable Long id) {
        try {
            byte[] pdfContent = pdfService.getPdfContent(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline().filename("preview.pdf").build());

            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Download a PDF file
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        try {
            MembershipConfirmation confirmation = pdfService.getConfirmationById(id);
            byte[] pdfContent = pdfService.getPdfContent(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename(confirmation.getFileName()).build());

            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a membership confirmation (soft delete)
     */
    @PostMapping("/delete/{id}")
    public String deleteConfirmation(@PathVariable Long id, Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            pdfService.deleteConfirmation(id, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Confirmation deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete confirmation: " + e.getMessage());
        }

        return "redirect:/admin/membership-confirmations";
    }
}
