package com.giovastk.stk500;

import com.giovastk.stk500.responses.STK500Response;

/**
 * Created by giova on 10/06/2016.
 */
// The callback interface
public interface STKCallback {
    void callbackCall(STK500Response rsp);
}

