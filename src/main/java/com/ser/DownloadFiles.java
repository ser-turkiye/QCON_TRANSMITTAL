package com.ser;

import com.ser.blueline.*;
import com.ser.blueline.bpm.IProcessInstance;
import com.ser.blueline.bpm.ITask;
import com.ser.blueline.bpm.IWorkbasketContent;
import com.ser.evITAWeb.EvitaWebException;
import com.ser.evITAWeb.api.IDoxisServer;
import com.ser.evITAWeb.api.actions.IBasicAction;
import com.ser.evITAWeb.api.actions.IExternalConfiguration;
import com.ser.evITAWeb.api.actions.IMessageAction;
import com.ser.evITAWeb.api.actions.IStopFurtherAction;
import com.ser.evITAWeb.api.toolbar.Button;
import com.ser.evITAWeb.scripting.Doxis4ClassFactory;
import com.ser.evITAWeb.scripting.bpmservice.workbasket.WorkbasketScripting;
import com.ser.evITAWeb.scripting.document.DocumentRibbonButtonAction;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


public class DownloadFiles extends WorkbasketScripting {
    public ISession ses;
    public DownloadFiles(){
        super();
        this.log=super.log;
    }
    //add a button on document View action
    @Override
    public void onInit() throws EvitaWebException {
        log.info("BB WEBCUBE WORKBASKET LOG START...ON INITTTTT");
        try {
            getRibbon().addButton(createExampleButton());
        } catch (EvitaWebException e) {
            throw new RuntimeException(e);
        }
    }
    //creating a button
    public Button createExampleButton() {
        String extensionName = getExtensionReference().extensionName;
        Button btn = Doxis4ClassFactory.getButton();
        btn.setImagePath("extensions/"+extensionName+"/images/smalldoxi.jpg");
        btn.setToolTip("Doxi");
        btn.setTitle("Download File");
        btn.setSynchronizedExecution(true);
        btn.setClassToExecute(DownloadFiles.OpenMessageBoxAction.class.getName());
        log.info("Button created");
        return btn;
    }
    //Open a message box when we click a button
    public static class OpenMessageBoxAction extends DocumentRibbonButtonAction {
        @Override
        public IBasicAction onClick(String[] selected) throws EvitaWebException {
            //Logger log = LoggerFactory.getLogger(DownloadFiles.class);
            List<IInformationObject> infoObjects = new ArrayList<>();
            String uniqueId = UUID.randomUUID().toString();
            String exportPath = "C:/tmp/" + uniqueId + "/Blobs";
            String downloadPath = "C:/ser/dx4Webcube/webapps/webcube/tmp/downloads/" + uniqueId;
            //String exportPath = getDoxisServer().getRequestURL() + "/tmp/downloads/" + uniqueId;
            String zipPath = "";
            log.info("start onClick:::exportPath : " + exportPath);
            try {
                loadDirectory("C:/tmp/" + uniqueId);
                loadDirectory(exportPath);
                loadDirectory(downloadPath);
                IMessageAction msg = Doxis4ClassFactory.createShowMessageAction();
                log.info("Export PAth:" + exportPath);
                if (selected != null && selected.length > 0) {
                    //msg = Doxis4ClassFactory.createShowMessageAction();
                    for (int i = 0; i < selected.length; i++) {
                        String id = selected[i];
                        log.info("selected ID:::" + id);
                        IInformationObject informationObject = getDocServer().getInformationObjectByID(id, getSession());
                        log.info("GET INFI OBJ NAME:" + informationObject.getDisplayName());
                        infoObjects.add(informationObject);
                    }
                    zipPath = getZipFile(infoObjects, exportPath, downloadPath, getDoxisServer(), log);
                    log.info("ZIP PATH:" + zipPath);
                    IExternalConfiguration conf = Doxis4ClassFactory.createOpenExternalConfiguration();
                    String exportURL = getDoxisServer().getRequestURL() + "/tmp/downloads/" + uniqueId + "/Blobs.zip";
                    log.info("EXPORT URL::" + exportURL);
                    try {
                        URL url = new URL(exportURL);
                        conf.getOpenURLConfiguration().setURL(url);
                        conf.setTargetFrame("evitawebtarget");
                    } catch (MalformedURLException e) {
                        throw new EvitaWebException(e);
                    }
                    return conf;
                    //msg.setMessage("selected item : " + selected.length);
                    //return msg;
                } else {
                    msg = Doxis4ClassFactory.createShowMessageAction();
                    msg.setMessage("No document selected");
                    return msg;
                }
            } catch (BlueLineException e) {
                log.error("exception", e);
                throw new EvitaWebException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    public void onFetchWorkbasketContent(IWorkbasketContent iWorkbasketContent) throws EvitaWebException {
        log.info("BB WEBCUBE WORKBASKET LOG START...");
    }
    public static void loadDirectory(String path) {
        (new File(path)).mkdir();
    }
    public static String getZipFile(List<IInformationObject> informationObjects, String exportPath, String downloadPath, IDoxisServer server, Logger LOG) throws Exception {
        LOG.info("START ZIP FILE.....::");
        String objectNumber = "", revNumber = "", folderName = "";
        List<String> expFilePaths = new ArrayList<>();
        for(IInformationObject informationObject : informationObjects){
            objectNumber = informationObject.getDescriptorValue("ccmPrjDocNumber");
            revNumber = informationObject.getDescriptorValue("ccmPrjDocRevision");
            LOG.info("DOCUMENT NUMBER:" + objectNumber);
            LOG.info("REV NUMBER:" + revNumber);
            folderName = (objectNumber != null ? objectNumber : "") + (revNumber != null ? "_" + revNumber : "");
            folderName = folderName.replaceAll("/","-");
            LOG.info("folderName:" + folderName);
            loadDirectory(exportPath + "/" + folderName);
            LOG.info("FULL PATH:" + exportPath + "/" + folderName);
            expFilePaths.add(exportPath + "/" + folderName);
            ITask task = (ITask) informationObject;
            //log.info("Get zip File....task:" + task.getName());
            IProcessInstance processInstance = task.getProcessInstance();
            IInformationObjectLinks links = processInstance.getLoadedInformationObjectLinks();
            for (ILink link : links.getLinks()) {
                IDocument edoc = (IDocument) link.getTargetInformationObject();
                LOG.info("Get zip File....linked edoc:" + edoc.getDisplayName());
                String expPath = exportDocument(edoc, exportPath + "/" + folderName);
                LOG.info("Get zip File....exported Path:" + expPath);
                if(expFilePaths.contains(expPath)){continue;}
                //log.info("IDOC [" + lcnt + "] *** " + edoc.getID());
                //expFilePaths.add(expPath);
            }
            IDatabase[] databases = informationObject.getSession().getDatabases();
            ILink[] links2 = server.getDocumentServer().getReferencedRelationships(informationObject.getSession(), informationObject,false, false, false);
            for (ILink link2 : links2) {
                IDocument edoc = (IDocument) link2.getTargetInformationObject();
                //log.info("Get zip File....linked edoc:" + edoc.getDisplayName());
                String expPath2 = exportDocument(edoc, exportPath);
                if(expFilePaths.contains(expPath2)){continue;}
                //log.info("IDOC [" + lcnt + "] *** " + edoc.getID());
                expFilePaths.add(expPath2);
            }
            IInformationObject[] links3 = server.getDocumentServer().getReferencingInformationObjects(informationObject.getSession(), informationObject,false,
                    VersionIdentifier.CURRENT_VERSION, IRetentionState.ALL,false,databases);
            for (IInformationObject link3 : links3) {
                IDocument edoc = (IDocument) link3;
                //log.info("Get zip File....linked edoc:" + edoc.getDisplayName());
                String expPath2 = exportDocument(edoc, exportPath);
                if(expFilePaths.contains(expPath2)){continue;}
                //log.info("IDOC [" + lcnt + "] *** " + edoc.getID());
                expFilePaths.add(expPath2);
            }
        }
        LOG.info("Get zip File....exportPath:" + exportPath);
        return zipFolder(downloadPath + "/Blobs.zip", exportPath, LOG);
    }
    public static String zipFolder(String zipPath, String exportPath, Logger LOG) throws IOException {
        LOG.info("zipFolder....zipPath:" + zipPath);
        LOG.info("zipFolder....exportPath:" + exportPath);
        FileOutputStream FoS = new FileOutputStream(zipPath);
        ZipOutputStream zipOut = new ZipOutputStream(FoS);
        File fileToZip = new File(exportPath);
        zipFile( fileToZip, fileToZip.getName( ), zipOut );
        zipOut.close();
        FoS.close();
        return zipPath;
    }
    public static String zipFiles(String zipPath, String pdfPath, List<String> expFilePaths, Logger LOG) throws IOException {
        if(expFilePaths.size() == 0){return "";}

        LOG.info("zipFiles....zipPath:" + zipPath);
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(new File(zipPath)));
        for (String expFilePath : expFilePaths) {
            LOG.info("zipFiles....expFilePath:" + expFilePath);;
//            String fileName = Paths.get(expFilePath).getFileName().toString();
//            fileName = fileName.replace("[@SLASH]", "/");
//            ZipEntry zlin = new ZipEntry(fileName);
//            zout.putNextEntry(zlin);
//            byte[] zdln = Files.readAllBytes(Paths.get(expFilePath));
//            zout.write(zdln, 0, zdln.length);
//            zout.closeEntry();
        }
        zout.close();
        return zipPath;
    }
    private static void zipFile( File fileToZip, String fileName, ZipOutputStream zipOut ) throws IOException {
        if (fileToZip.isHidden()) {
            return ;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName)) ;
                zipOut.closeEntry() ;
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/" )) ;
                zipOut.closeEntry() ;
            }
            File[] children = fileToZip.listFiles() ;
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut) ;
            }
            return ;
        }
        FileInputStream FiS = new FileInputStream(fileToZip) ;
        ZipEntry zipEntry = new ZipEntry(fileName) ;
        zipOut.putNextEntry(zipEntry) ;
        byte[] bytes = new byte[ 1024 ] ;
        int length ;
        while ((length = FiS.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length) ;
        }
        FiS.close();
    }
    public static String exportDocument(IDocument document, String exportPath) throws IOException {
        String rtrn ="";
        loadDirectory(exportPath);
        IDocumentPart partDocument = document.getPartDocument(document.getDefaultRepresentation() , 0);

        String fName = partDocument.getFilename();
        fName = fName.replaceAll("[\\\\/:*?\"<>|]", "_");

        try (InputStream inputStream = partDocument.getRawDataAsStream()) {
            IFDE fde = partDocument.getFDE();
            if (fde.getFDEType() == IFDE.FILE) {
                //rtrn = exportPath + "/" + fName + "." + ((IFileFDE) fde).getShortFormatDescription();
                rtrn = exportPath + "/" + fName;

                try (FileOutputStream fileOutputStream = new FileOutputStream(rtrn)){
                    byte[] bytes = new byte[2048];
                    int length;
                    while ((length = inputStream.read(bytes)) > -1) {
                        fileOutputStream.write(bytes, 0, length);
                    }
                }
            }
        }
        return rtrn;
    }
}
