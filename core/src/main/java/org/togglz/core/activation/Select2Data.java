package org.togglz.core.activation;

import java.io.Serializable;

public class Select2Data implements Serializable, Comparable<Select2Data>
{
    private static final long serialVersionUID = 2795524522987532616L;

    private String id;
    private String image; //can hold a font-awesome icon html
    private String text;
    private String meta1;
    private String meta2;
    private String html;

    public Select2Data(String id, String text)
    {
        this.id = id;
        this.text = text;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public String getMeta1()
    {
        return meta1;
    }

    public void setMeta1(String meta1)
    {
        this.meta1 = meta1;
    }

    public String getMeta2()
    {
        return meta2;
    }

    public void setMeta2(String meta2)
    {
        this.meta2 = meta2;
    }

    public String getHtml()
    {
        return html;
    }

    public void setHtml(String html)
    {
        this.html = html;
    }

    @Override
    public int compareTo(Select2Data other)
    {
        int textCompare = other.getText().toLowerCase().compareTo(getText().toLowerCase());
        return textCompare == 0 ? other.getId().compareTo(getId()) : textCompare;
    }
}
