package control;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import entity.CareerCenterStaff;
import entity.CompanyRepresentative;
import entity.InternshipOpportunity;
import enumerations.Major;
import enumerations.OpportunityStatus;
import ui.ConsoleUI;

public class OpportunityService {

    private final List<InternshipOpportunity> opportunities;
    private final DataLoader loader; // for persistence

    public OpportunityService(List<InternshipOpportunity> opportunities, DataLoader loader) {
        this.opportunities = Objects.requireNonNull(opportunities, "Opportunities must not be null");
        this.loader = Objects.requireNonNull(loader, "Loader must not be null");
    }

    // -------------------- company rep actions --------------------

    /** create a new internship opportunity (auto-generates unique id and rejects duplicates) */
    public boolean createOpportunity(CompanyRepresentative rep, InternshipOpportunity opp) {
        if (rep == null || opp == null) {
            System.out.println("✗ Invalid data.");
            return false;
        }

        // generate a unique internship id in the format ITP-xxxxxx
        String newId;
        do {
            newId = "ITP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        } while (findById(newId) != null); // ensure no duplicates

        opp.setId(newId); // assign generated id

        opp.setRepInCharge(rep);
        opp.setStatus(OpportunityStatus.PENDING);
        opp.setVisibility(false);

        opportunities.add(opp);
        save();
        System.out.println();
        System.out.println("✓ Internship opportunity created with ID: " + newId);
        System.out.println("✓ Awaiting Career Staff approval.");
        return true;
    }

    /** edit an existing opportunity (only if rep owns it) */
    public void editOpportunity(CompanyRepresentative rep, InternshipOpportunity updated) {
        InternshipOpportunity existing = findById(updated.getId());

        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        Major newMajor = updated.getPreferredMajor();
        existing.setPreferredMajor(newMajor);

        existing.setLevel(updated.getLevel());
        existing.setOpenDate(updated.getOpenDate());
        existing.setCloseDate(updated.getCloseDate());
        existing.setSlots(updated.getSlots());

        // any edit requires re-approval
        existing.setStatus(OpportunityStatus.PENDING);
        existing.setVisibility(false);

        save();
        System.out.println("✓ Opportunity updated and sent for re-approval.");
    }

    /** delete an opportunity (only by the owning rep and only if status is pending) */
    public void deleteOpportunity(CompanyRepresentative rep, String id) {
        InternshipOpportunity existing = findById(id);

        opportunities.remove(existing);
        save();
        System.out.println("✓ Opportunity deleted.");
    }

    /** toggle visibility (call from appropriate ui; no ownership check here) */
    public void toggleVisibility(InternshipOpportunity opp, boolean visible) {
        if (opp == null) {
            System.out.println("✗ Opportunity not found.");
            return;
        }
        opp.setVisibility(visible);
        save();
        System.out.println(visible ? "✓ Now visible to students." : "✓ Hidden from students.");
    }

    // -------------------- lookups --------------------

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

    /** filter by preferred major (enum) */
    public List<InternshipOpportunity> getByMajor(Major major) {
        return opportunities.stream()
                .filter(o -> o.getPreferredMajor() == major)
                .collect(Collectors.toList());
    }

    /** convenience getters */
    public List<InternshipOpportunity> getAllOpportunities() { return opportunities; }

    public List<InternshipOpportunity> getPending() {
        return opportunities.stream()
            .filter(o -> o.getStatus() == OpportunityStatus.PENDING)
            .collect(Collectors.toList());
    }

    public List<InternshipOpportunity> getApproved() {
        return opportunities.stream()
            .filter(o -> o.getStatus() == OpportunityStatus.APPROVED)
            .collect(Collectors.toList());
    }

    // -------------------- staff actions --------------------

    /** approve an opportunity */
    public void approveOpportunity(CareerCenterStaff staff, InternshipOpportunity opp) {
        opp.setStatus(OpportunityStatus.APPROVED);
        opp.setVisibility(true);
        save();
        System.out.println("✓ approved by " + staff.getName() + ".");
    }

    /** reject an opportunity (no reason) */
    public void rejectOpportunity(CareerCenterStaff staff, InternshipOpportunity opp) {
        opp.setStatus(OpportunityStatus.REJECTED);
        opp.setVisibility(false);
        save();
        System.out.println("✓ rejected by " + staff.getName() + ".");
    }

    // -------------------- system utilities --------------------

    /** check if all slots are filled and update status accordingly */
    public void recomputeFilledStatus(InternshipOpportunity opp) {
        if (opp.getConfirmedSlots() >= opp.getSlots()) {
            opp.setStatus(OpportunityStatus.FILLED);
            opp.setVisibility(false);
        } else if (opp.getStatus() == OpportunityStatus.FILLED) {
            opp.setStatus(OpportunityStatus.APPROVED);
        }
        save();
    }

    // -------------------- persistence --------------------

    private void save() {
        loader.saveOpportunities(opportunities);
    }
}