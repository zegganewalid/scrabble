public enum Direction {
    HORIZONTAL("Horizontal"),
    VERTICAL("Vertical");

    private final String name;

    Direction(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}