package entity;

/** Per-user saved UI/filter settings (composition from User). */
public class UserPreferences {
    private FilterCriteria lastFilter;  // value object (add class if needed later)

    public FilterCriteria getLastFilter()              { return lastFilter; }
    public void setLastFilter(FilterCriteria lastFilter){ this.lastFilter = lastFilter; }
}
