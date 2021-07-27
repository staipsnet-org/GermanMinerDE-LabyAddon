package de.germanminer.addon.utils;

public class Location implements Cloneable {
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;

    public Location(double x, double y, double z) {
        this(x, y, z, 0.0F, 0.0F);
    }

    public Location(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getX() {
        return this.x;
    }

    public int getBlockX() {
        return locToBlock(this.x);
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getY() {
        return this.y;
    }

    public int getBlockY() {
        return locToBlock(this.y);
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getZ() {
        return this.z;
    }

    public int getBlockZ() {
        return locToBlock(this.z);
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getPitch() {
        return this.pitch;
    }

    public Location add(Location vec) {
        if (vec != null) {
            this.x += vec.x;
            this.y += vec.y;
            this.z += vec.z;
            return this;
        } else {
            throw new IllegalArgumentException("Cannot add Locations of differing worlds");
        }
    }

    public Location add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Location subtract(Location vec) {
        if (vec != null) {
            this.x -= vec.x;
            this.y -= vec.y;
            this.z -= vec.z;
            return this;
        } else {
            throw new IllegalArgumentException("Cannot add Locations of differing worlds");
        }
    }

    public Location subtract(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public double distance(Location o) {
        return Math.sqrt(this.distanceSquared(o));
    }

    public double distanceSquared(Location o) {
        if (o == null) {
            throw new IllegalArgumentException("Cannot measure distance to a null location");
        } else {
            return Math.pow(this.x - o.x, 2) + Math.pow(this.y - o.y, 2) + Math.pow(this.z - o.z, 2);
        }

    }

    public Location multiply(double m) {
        this.x *= m;
        this.y *= m;
        this.z *= m;
        return this;
    }

    public Location zero() {
        this.x = 0.0D;
        this.y = 0.0D;
        this.z = 0.0D;
        return this;
    }

    public Location set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Location add(Location base, double x, double y, double z) {
        return this.set(base.x + x, base.y + y, base.z + z);
    }

    public Location subtract(Location base, double x, double y, double z) {
        return this.set(base.x - x, base.y - y, base.z - z);
    }

    public Location toBlockLocation() {
        Location blockLoc = this.clone();
        blockLoc.setX((double) this.getBlockX());
        blockLoc.setY((double) this.getBlockY());
        blockLoc.setZ((double) this.getBlockZ());
        return blockLoc;
    }

    public long toBlockKey() {
        return (long) this.getBlockX() & 134217727L | ((long) this.getBlockZ() & 134217727L) << 27 | (long) this.getBlockY() << 54;
    }

    public Location toCenterLocation() {
        Location centerLoc = this.clone();
        centerLoc.setX((double) this.getBlockX() + 0.5D);
        centerLoc.setY((double) this.getBlockY() + 0.5D);
        centerLoc.setZ((double) this.getBlockZ() + 0.5D);
        return centerLoc;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            Location other = (Location) obj;
            if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
                return false;
            } else if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
                return false;
            } else if (Double.doubleToLongBits(this.z) != Double.doubleToLongBits(other.z)) {
                return false;
            } else if (Float.floatToIntBits(this.pitch) != Float.floatToIntBits(other.pitch)) {
                return false;
            } else {
                return Float.floatToIntBits(this.yaw) == Float.floatToIntBits(other.yaw);
            }
        }
    }

    public int hashCode() {
        int hash = 19 * 3;
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.x) ^ Double.doubleToLongBits(this.x) >>> 32);
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.y) ^ Double.doubleToLongBits(this.y) >>> 32);
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.z) ^ Double.doubleToLongBits(this.z) >>> 32);
        hash = 19 * hash + Float.floatToIntBits(this.pitch);
        hash = 19 * hash + Float.floatToIntBits(this.yaw);
        return hash;
    }

    public String toString() {
        return "Location{x=" + this.x + ",y=" + this.y + ",z=" + this.z + ",pitch=" + this.pitch + ",yaw=" + this.yaw + '}';
    }

    public Location clone() {
        try {
            return (Location) super.clone();
        } catch (CloneNotSupportedException var2) {
            throw new Error(var2);
        }
    }

    public static int locToBlock(double loc) {
        return (int) Math.floor(loc);
    }
}

