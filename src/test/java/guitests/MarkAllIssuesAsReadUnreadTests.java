package guitests;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import org.junit.Before;
import org.junit.Test;

import ui.components.FilterTextField;
import ui.components.IssueListView;
import ui.listpanel.ListPanel;
import ui.listpanel.ListPanelCell;
import util.PlatformEx;

import java.lang.reflect.*;

import static org.junit.Assert.assertEquals;

public class MarkAllIssuesAsReadUnreadTests extends UITest {

    private static final int EVENT_DELAY = 1000;

    @Before
    public void setupUIComponent() {
        PlatformEx.runAndWait(getStage()::requestFocus);

        FilterTextField filterTextField = getFilterTextFieldAtPanel(0);
        filterTextField.setText("");

        clickFilterTextFieldAtPanel(0);
        push(KeyCode.ENTER);
    }

    /**
     * Tests selecting "Mark all below as read" and "Mark all below as unread" context menu items
     */
    @Test
    public void markAllBelowAsReadUnread_multipleIssuesInListView_issuesCorrectlyMarked()
            throws NoSuchFieldException, IllegalAccessException {

        ListPanel issuePanel = getPanel(0);
        Field listViewField = ListPanel.class.getDeclaredField("listView");
        listViewField.setAccessible(true);
        IssueListView listViewValue = (IssueListView) listViewField.get(issuePanel);

        // scrolls to the end of the panel
        listViewValue.scrollAndShow(issuePanel.getIssuesCount());


        //checking for issue #7 and below as marked read
        clickAndMarkIssuesBelow(issuePanel, 7, true);
        verifyReadStatusOfIssuesBelow(7, true);

        //checking for issue #7 and below as marked read again to ensure they remain read even on a repeated command
        clickAndMarkIssuesBelow(issuePanel, 7, true);
        verifyReadStatusOfIssuesBelow(7, true);

        //checking for issue #7 and below as marked unread
        clickAndMarkIssuesBelow(issuePanel, 7, false);
        verifyReadStatusOfIssuesBelow(7, false);

        //checking for issue #7 and below as marked unread again to ensure they remain unread
        // even on a repeated command
        clickAndMarkIssuesBelow(issuePanel, 7, false);
        verifyReadStatusOfIssuesBelow(7, false);

        //checking for the last issue as marked read
        clickAndMarkIssuesBelow(issuePanel, 1, true);
        verifyReadStatusOfIssuesBelow(1, true);

        //checking for the last issue as marked unread
        clickAndMarkIssuesBelow(issuePanel, 1, false);
        verifyReadStatusOfIssuesBelow(1, false);
    }

    /**
     * Marks issues in the panel from Issue index in the UI right to the end of the list as read/unread
     *
     * @param isMarkAsRead If true, marks the selected issue and below as read
     *                     If false, marks the selected issue and below as unread
     * @param index        The issue number in the panel
     */
    private void clickAndMarkIssuesBelow(ListPanel issuePanel, int index, boolean isMarkAsRead) {
        clickIssue(0, index);
        rightClickIssue(0, index);
        ContextMenu contextMenu = issuePanel.getContextMenu();
        for (MenuItem menuItem : contextMenu.getItems()) {
            awaitCondition(menuItem::isVisible);
        }

        if (isMarkAsRead){
            clickMenuItem(contextMenu, ListPanel.MARK_ALL_AS_READ_MENU_ITEM_TEXT);
        } else {
            clickMenuItem(contextMenu, ListPanel.MARK_ALL_AS_UNREAD_MENU_ITEM_TEXT);
        }
        sleep(EVENT_DELAY);
    }

    /**
     * Tests whether list panel cells corresponding to a particular index and below are marked as read/unread
     */
    private void verifyReadStatusOfIssuesBelow(int index, boolean isExpectedStatusRead) {
        for (int i = index; i >= 1; i--) {
            ListPanelCell listPanelCell = getIssueCell(0, index);
            assertEquals(isExpectedStatusRead, listPanelCell.getIssue().isCurrentlyRead());
        }
    }
}
