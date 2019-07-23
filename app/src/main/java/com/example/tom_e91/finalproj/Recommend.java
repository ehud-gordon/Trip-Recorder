package com.example.tom_e91.finalproj;

public class Recommend
{
    // Fields
    private static long currId = 0;
    private long id;
    private String title;


    public Recommend (String title)
    {
        this.title = title;
        this.id = currId;
        currId++;
    }

    public String getTitle() {return title;}

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recommend recommend = (Recommend) o;
        return recommend.title.equals(this.title);
    }
}
