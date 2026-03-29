package com.ruiyun.example;

import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.ElementHandle;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.cdp.core.AXNode;
import com.ruiyun.jvppeteer.cdp.core.Accessibility;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.SerializedAXNode;
import com.ruiyun.jvppeteer.cdp.entities.SnapshotOptions;
import org.junit.Test;


import java.util.List;

import static com.ruiyun.example.LaunchTest.LAUNCHOPTIONS;

public class AccessibilityApiTest {

    @Test
    public void test3() throws Exception {
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        Page page = browser.newPage();
        page.setContent("<!DOCTYPE html>\n" +
                "      <html lang=\"en\">\n" +
                "      <head>\n" +
                "        <meta charset=\"UTF-8\">\n" +
                "        <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "        <title>Accessible name + aria-expanded puppeteer bug</title>\n" +
                "        <style>\n" +
                "          [aria-expanded=\"false\"] + * {\n" +
                "            display: none;\n" +
                "          }\n" +
                "        </style>\n" +
                "      </head>\n" +
                "      <body>\n" +
                "        <button hidden>Show</button>\n" +
                "        <p>Some content</p>\n" +
                "        <script>\n" +
                "          const button = document.querySelector('button');\n" +
                "          button.removeAttribute('hidden')\n" +
                "          button.setAttribute('aria-expanded', 'false');\n" +
                "          button.addEventListener('click', function() {\n" +
                "            button.setAttribute('aria-expanded', button.getAttribute('aria-expanded') !== 'true')\n" +
                "            if (button.getAttribute('aria-expanded') == 'true') {\n" +
                "              button.textContent = 'Hide'\n" +
                "            } else {\n" +
                "              button.textContent = 'Show'\n" +
                "            }\n" +
                "          })\n" +
                "        </script>\n" +
                "      </body>\n" +
                "      </html>");
        ElementHandle button = page.$("button");
        SerializedAXNode snapshot = page.accessibility().snapshot(new SnapshotOptions(true, button, false));
        System.out.println(snapshot);
        button.click();
        ElementHandle elementHandle = page.waitForSelector("aria/Hide");
        System.out.println(elementHandle);
    }

    @Test
    public void test4() throws Exception {
        Browser browser = Puppeteer.launch(LAUNCHOPTIONS);
        Page page = browser.newPage();
        // 设置页面内容
        String htmlContent = "<div role=\"alert\" aria-busy=\"true\">This is an alert</div>\n" +
                "<div aria-live=\"polite\" aria-atomic=\"true\" aria-relevant=\"additions text\">\n" +
                "  This is polite live region\n" +
                "</div>\n" +
                "<div aria-modal=\"true\" role=\"dialog\" aria-roledescription=\"My Modal\">\n" +
                "  Modal content\n" +
                "</div>\n" +
                "<div id=\"error\">Error message</div>\n" +
                "<input aria-invalid=\"true\" aria-errormessage=\"error\" value=\"invalid input\">\n" +
                "<div id=\"details\">Additional details</div>\n" +
                "<div aria-details=\"details\">Element with details</div>\n" +
                "<div aria-description=\"This is a description\"></div>";

        page.setContent(htmlContent);
        // 获取可访问性快照
        Accessibility accessibility = page.accessibility();
        SerializedAXNode snapshot = accessibility.snapshot();

        // 验证快照结果
        validateAccessibilitySnapshot(snapshot);

        // 关闭浏览器
        browser.close();
    }
    private static void validateAccessibilitySnapshot(SerializedAXNode snapshot) {
        // 验证根节点
        assert "RootWebArea".equals(snapshot.getRole()) : "Root node should have role 'RootWebArea'";

        List<SerializedAXNode> children = snapshot.getChildren();
        assert children != null && !children.isEmpty() : "Should have children nodes";

        // 检查第一个子节点 (alert)
        SerializedAXNode alertNode = children.get(0);
        assert "alert".equals(alertNode.getRole()) : "First node should have role 'alert'";
        assert "".equals(alertNode.getName()) : "Alert node name should be empty";
        assert Boolean.TRUE.equals(alertNode.getBusy()) : "Alert node should have busy=true";
        assert "assertive".equals(alertNode.getLive()) : "Alert node should have live='assertive'";
        assert Boolean.TRUE.equals(alertNode.getAtomic()) : "Alert node should have atomic=true";

        // 检查第二个子节点 (live region)
        SerializedAXNode liveRegionNode = children.get(1);
        assert "generic".equals(liveRegionNode.getRole()) : "Second node should have role 'generic'";
        assert "polite".equals(liveRegionNode.getLive()) : "Live region should have live='polite'";
        assert Boolean.TRUE.equals(liveRegionNode.getAtomic()) : "Live region should have atomic=true";
        assert "additions text".equals(liveRegionNode.getRelevant()) : "Live region should have relevant='additions text'";

        // 检查第三个子节点 (dialog)
        SerializedAXNode dialogNode = children.get(2);
        assert "dialog".equals(dialogNode.getRole()) : "Third node should have role 'dialog'";
        assert Boolean.TRUE.equals(dialogNode.getModal()) : "Dialog node should have modal=true";
        assert "My Modal".equals(dialogNode.getRoledescription()) : "Dialog should have roledescription='My Modal'";

        // 检查第四个子节点 (error message)
        SerializedAXNode errorTextNode = children.get(3);
        assert "StaticText".equals(errorTextNode.getRole()) : "Fourth node should have role 'StaticText'";
        assert "Error message".equals(errorTextNode.getName()) : "Error text should have name 'Error message'";

        // 检查第五个子节点 (textbox)
        SerializedAXNode textboxNode = children.get(4);
        assert "textbox".equals(textboxNode.getRole()) : "Fifth node should have role 'textbox'";
        assert "invalid input".equals(textboxNode.getValue()) : "Textbox should have value 'invalid input'";
        assert "true".equals(textboxNode.getInvalid()) : "Textbox should have invalid='true'";
        assert "error".equals(textboxNode.getErrormessage()) : "Textbox should have errormessage='error'";

        // 检查第六个子节点 (details text)
        SerializedAXNode detailsTextNode = children.get(5);
        assert "StaticText".equals(detailsTextNode.getRole()) : "Sixth node should have role 'StaticText'";
        assert "Additional details".equals(detailsTextNode.getName()) : "Details text should have name 'Additional details'";

        // 检查第七个子节点 (element with details)
        SerializedAXNode elementWithDetailsNode = children.get(6);
        assert "generic".equals(elementWithDetailsNode.getRole()) : "Seventh node should have role 'generic'";
        assert "details".equals(elementWithDetailsNode.getDetails()) : "Element should have details='details'";

        // 检查第八个子节点 (element with description)
        SerializedAXNode elementWithDescriptionNode = children.get(7);
        assert "generic".equals(elementWithDescriptionNode.getRole()) : "Eighth node should have role 'generic'";
        assert "This is a description".equals(elementWithDescriptionNode.getDescription()) : "Element should have description='This is a description'";

        System.out.println("所有可访问性断言都已通过！");
    }

}
