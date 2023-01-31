package com.serverless;

public class ViewSpotsRequest {
    int number;
    Mesh mesh;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    public ViewSpotsRequest() {
    }
}
