package control;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import entity.CareerCenterStaff;
import entity.CompanyRepresentative;
import entity.InternshipOpportunity;
import enumerations.InternshipLevel;
import enumerations.OpportunityStatus;
import ui.ConsoleUI;

/**
 * Handles creation, editing, approval and maintenance of internship opportunities.
 */
public class OpportunityService {

    private final List<InternshipOpportunity> opportunities;

    public OpportunityService(List<InternshipOpportunity> opportunities) {
        this.opportunities = Objects.requireNonNull(opportunities);
    }

    // -------------------- COMPANY REP ACTIONS --------------------

    /** Create a new internship opportunity */
    public void createOpportunity(CompanyRepresentative rep, InternshipOpportunity opp) {
        opp.setRepInCharge(rep);
        opp.setStatus(OpportunityStatus.PENDING);
        opp.setVisibility(false);
        opportunities.add(opp);
        System.out.println("✓ Opportunity created and awaiting staff approval.");
    }

    /** Edit an existing opportunity (only if rep owns it and it’s editable) */
   public void editOpportunity(CompanyRepresentative rep, InternshipOpportunity updated) {
    InternshipOpportunity existing = findById(updated.getId());
    if (existing == null) {
        System.out.println("✗ Opportunity not found.");
        return;
    }
    if (!rep.equals(existing.getRepInCharge())) {
        System.out.println("✗ You may only edit your own opportunities.");
        return;
    }

    existing.setTitle(updated.getTitle());
    existing.setDescription(updated.getDescription());
    existing.setPreferredMajor(updated.getPreferredMajor());
    existing.setLevel(updated.getLevel());
    existing.setOpenDate(updated.getOpenDate());
    existing.setCloseDate(updated.getCloseDate());
    existing.setSlots(updated.getSlots());
    existing.setStatus(OpportunityStatus.PENDING);
    existing.setVisibility(false);

    System.out.println("✓ Opportunity updated and sent for re-approval.");
}



    /** Delete an opportunity (only by the owning rep) */
    public void deleteOpportunity(CompanyRepresentative rep, String id) {
        InternshipOpportunity existing = findById(id);
        if (existing == null) {
            System.out.println("✗ Opportunity not found.");
            return;
        }
        if (!rep.equals(existing.getRepInCharge())) {
            System.out.println("✗ You may only delete your own opportunities.");
            return;
        }
        opportunities.remove(existing);
        System.out.println("✓ Opportunity deleted.");
    }

    /** Toggle visibility */
    public void toggleVisibility(InternshipOpportunity opp, boolean visible) {
        opp.setVisibility(visible);
        System.out.println(visible ? "✓ Now visible to students." : "✓ Hidden from students.");
    }


    public InternshipOpportunity findById(String id) {
        return opportunities.stream()
                .filter(o -> o.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    public List<InternshipOpportunity> getByCompany(String companyName) {
        return opportunities.stream()
                .filter(o -> o.getCompanyName().equalsIgnoreCase(companyName))
                .collect(Collectors.toList());
    }

    // -------------------- STAFF ACTIONS --------------------

    /** Approve an opportunity */
    public void approveOpportunity(CareerCenterStaff staff, InternshipOpportunity opp) {
        opp.setStatus(OpportunityStatus.APPROVED);
        opp.setVisibility(true);
        System.out.println("✓ Approved by " + staff.getName() + ".");
    }

    /** Reject an opportunity */
    public void rejectOpportunity(CareerCenterStaff staff, InternshipOpportunity opp, String reason) {
        opp.setStatus(OpportunityStatus.REJECTED);
        opp.setVisibility(false);
        System.out.println("✗ Rejected by " + staff.getName() + ". Reason: " + reason);
    }

    // -------------------- SYSTEM UTILITIES --------------------

    /** Check if all slots are filled and update status accordingly */
    public void recomputeFilledStatus(InternshipOpportunity opp) {
        if (opp.getConfirmedSlots() >= opp.getSlots()) {
            opp.setStatus(OpportunityStatus.FILLED);
            opp.setVisibility(false);
        } else if (opp.getStatus() == OpportunityStatus.FILLED) {
            opp.setStatus(OpportunityStatus.APPROVED);
        }
    }

}
