package fi.gtrxac.bluewap.client;

import fi.gtrxac.bluewap.ui.*;

public class WmlOptionGroup extends RadioButtonGroup {
    private String variableName;
    private String indexVariableName;
    
    public WmlOptionGroup(String varName, String iVarName) {
        variableName = varName;
        indexVariableName = iVarName;
    }

    public void setTicked(RadioButtonItem i) {
        super.setTicked(i);

        if (variableName != null) {
            WmlVariables.set(variableName, ((WmlOptionItem) i).value);

            // System.out.println("SET '" + variableName + "' TO '" + ((WmlOptionItem) i).value + "'");
        }
        if (indexVariableName != null) {
            String indexStr = Integer.toString(getTickedIndex());
            WmlVariables.set(indexVariableName, indexStr);

            // System.out.println("SET '" + indexVariableName + "' TO '" + indexStr + "'");
        }
    }

    public void setTickedValue(String value) {
        for (int i = 0; i < buttons.size(); i++) {
            WmlOptionItem button = (WmlOptionItem) buttons.elementAt(i);
            if (!value.equals(button.value)) continue;
            
            setTickedIndex(i);
            return;
        }
    }
}