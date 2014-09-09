/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package insp;

/**
 *
 * @author dasha
 */
public enum Status {
    ACTIVE("доступен"),
    INACTIVE("недоступен");
    
    private String value;

    private Status(String value) {
        this.value = value;
    }
    public String getValue() {
        return this.value;
    }   
}
