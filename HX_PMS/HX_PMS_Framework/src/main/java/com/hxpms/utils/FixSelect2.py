path = 'src/test/java/com/hxpms/stepdefinitions/BookingSteps.java'
with open(path, 'r') as f:
    content = f.read()

start = 14891
end = 17008

new_method = '''    // ── Select2 helper: jQuery val().trigger('change') ────────────────────────
    // XPath ref: //*[@id="select2-country-dropdown-container"]
    private void selectSelect2(WebDriverWait wait, String selectId, String optionText)
            throws InterruptedException {
        try {
            // Find option value matching the text
            String optionValue = (String) js.executeScript(
                "var sel=document.getElementById(arguments[0]);" +
                "for(var i=0;i<sel.options.length;i++){" +
                "  if(sel.options[i].text.trim().toLowerCase().indexOf(arguments[1].toLowerCase())>=0)" +
                "    return sel.options[i].value;" +
                "} return '';",
                selectId, optionText);

            if (optionValue == null || optionValue.trim().isEmpty()) {
                System.out.println("[BookingSteps] No option for '" + optionText + "' in #" + selectId);
                return;
            }

            // Set value via jQuery and trigger change
            js.executeScript(
                "var el=document.getElementById(arguments[0]);" +
                "el.value=arguments[1];" +
                "el.dispatchEvent(new Event('change',{bubbles:true}));" +
                "if(window.jQuery){jQuery('#'+arguments[0]).val(arguments[1]).trigger('change');}",
                selectId, optionValue);
            Thread.sleep(1000);

            String selected = (String) js.executeScript(
                "return document.getElementById(arguments[0]).value;", selectId);
            System.out.println("[BookingSteps] Select2 #" + selectId + " = '" + optionText + "' (value=" + selected + ")");
        } catch (Exception e) {
            System.out.println("[BookingSteps] selectSelect2 failed [" + selectId + "]: " + e.getMessage());
        }
    }

    '''

content = content[:start] + new_method + content[end:]
with open(path, 'w') as f:
    f.write(content)
print("REPLACED OK")
