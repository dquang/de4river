/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.types.Encoding;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.HTMLPane;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.UploadItem;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;


public class UserRGDProvider
extends SelectProvider
{

    private HTMLPane uploadTargetFrame;
    private String uploadFile;

    public UserRGDProvider() {
        uploadTargetFrame = new HTMLPane();
    }

    @Override
    public Canvas create(DataList list) {
        List<Data> data = list.getAll();

        //Canvas selectBox = super.create(clone);
        Canvas widget = createWidget(list);

        return widget;
    }


    /**
     * This method creates the content of the widget.
     *
     * @param data The {@link DataList} object.
     *
     * @return a combobox.
     */
    @Override
    protected Canvas createWidget(DataList data) {
        GWT.log("DigitizePanel - createWidget()");

        VLayout layout   = new VLayout();
        layout.setAlign(VerticalAlignment.TOP);
        layout.setHeight(25);

        int size = data.size();

        for (int i = 0; i < size; i++) {
            Data d = data.get(i);

            Label label = new Label(d.getDescription());
            label.setValign(VerticalAlignment.TOP);
            label.setHeight(20);
            label.setWidth(400);

            uploadTargetFrame.setWidth("200px");
            uploadTargetFrame.setHeight("50px");
            uploadTargetFrame.setContents(
                "<iframe id='uploadTarget' name='uploadTarget' "
                + "scrolling='no' width=200 height=50 "
                + "style='border: 0px'></iframe>");
            uploadTargetFrame.setBorder("0px");
            uploadTargetFrame.setScrollbarSize(0);

            final DynamicForm uploadForm = new DynamicForm();
            uploadForm.setAction("flys/fileupload?uuid=" + artifact.getUuid());
            uploadForm.setTarget("uploadTarget");
            uploadForm.setEncoding(Encoding.MULTIPART);
            Label uploadLabel = new Label(MSG.shape_file_upload());
            uploadLabel.setHeight(20);
            final UploadItem uploadItem = new UploadItem();
            uploadItem.setShowTitle(false);
            uploadForm.setFields(uploadItem);
            Button submit = new Button(MSG.upload_file());
            submit.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent e) {
                    uploadFile = uploadItem.getValueAsString();
                    uploadForm.submitForm();
                }
            });

            layout.addMember(label);
            layout.addMember(form);
            layout.addMember(uploadLabel);
            layout.addMember(uploadForm);
            layout.addMember(submit);
            layout.addMember(getNextButton());

            layout.setMembersMargin(10);
            layout.addMember(uploadTargetFrame);
        }

        layout.setAlign(VerticalAlignment.TOP);

        return layout;
    }

    @Override
    protected Data[] getData() {
        Data[] total = new Data[1];

        if (uploadFile != null && uploadFile.length() > 0) {
            DataItem item = new DefaultDataItem(
                "uesk.user-rgd", "uesk.user-rgd", uploadFile);
            total[0] = new DefaultData(
                "uesk.user-rgd", null, null, new DataItem[] { item });
        }
        else {
            // Happens when OpenLayers is missing
            DataItem item = new DefaultDataItem(
                "uesk.user-rgd", "uesk.user-rgd", MSG.notselected());
            total[0] = new DefaultData(
                "uesk.user-rgd", null, null, new DataItem[] { item });
        }

        return total;
    }
}
