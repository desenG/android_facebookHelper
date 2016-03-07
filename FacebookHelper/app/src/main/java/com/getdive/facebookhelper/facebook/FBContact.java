package com.getdive.facebookhelper.facebook;

import java.io.Serializable;

/**
 * Created by desenguo on 2016-03-06.
 */
public class FBContact implements Serializable {

    private static final long serialVersionUID = 1L;

    public int userID = 0;
    public String name;
    public String emailAddress;
    public String phone;
    public boolean isChecked = false;
    public String facebookFriendID;
}
