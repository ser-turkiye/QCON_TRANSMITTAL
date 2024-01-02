package utils;

import com.ser.blueline.*;
import com.ser.blueline.metaDataComponents.IStringMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GeneralLib {

    public static class ClassIDs{
        public static final String ProjectWorkspace = "32e74338-d268-484d-99b0-f90187240549";
        public static final String Contact = "d7ffea9d-3419-4922-8ffa-a0310add5723";
        public static final String EngineeringDocument = "3b3078f8-c0d0-4830-b963-88bebe1c1462";
        public static final String DocMainReview = "69d42aaf-6978-4b5a-8178-88a78f4b3158";
        public static final String InvolveParty = "fad93754-b7c2-4a12-b40e-8afae3b31e3d";
    }
    public static class Databases{
        public static final String ProjectWorkspace = "PRJ_FOLDER";
        public static final String SupplierContact = "BPWS";
        public static final String InvolveParty = "PRJ_FOLDER";
    }
    public static class Descriptors{
        public static final String ProjectNo = "ccmPRJCard_code";
        public static final String ProjectName = "ccmPRJCard_name";
        public static final String DocStatus = "ccmPrjDocStatus";
        public static final String EMail = "PrimaryEMail";
        public static final String FileName = "ccmPrjDocFileName";
    }
    public static class CheckValues{
        public static final List<String> SendDocStatuses = new ArrayList<>(Arrays.asList(
                "10"
        ));

    }
    public static ISession ses;
    private static final Logger log = LoggerFactory.getLogger(GeneralLib.class);
    public static void setSes(ISession ses) {
        GeneralLib.ses = ses;
    }

    public static IDocument getProjectCard(ISession s, String pCode)  {
        setSes(s);
        StringBuilder builder = new StringBuilder();
        builder.append("TYPE = '").append(ClassIDs.ProjectWorkspace).append("'")
                .append(" AND ")
                .append(Descriptors.ProjectNo).append(" = '").append(pCode).append("'");
        String whereClause = builder.toString();
        System.out.println("Where Clause: " + whereClause);

        IInformationObject[] informationObjects = createQuery(s, new String[] {Databases.ProjectWorkspace} , whereClause , 1);
        if(informationObjects.length < 1) {return null;}
        return (IDocument) informationObjects[0];
    }
    public static IDocument getContactRecord(ISession ses, String eMail)  {
        StringBuilder builder = new StringBuilder();
        builder.append("TYPE = '").append(ClassIDs.Contact).append("'")
                .append(" AND ")
                .append(Descriptors.EMail).append(" = '").append(eMail).append("'");
        String whereClause = builder.toString();
        System.out.println("Where Clause: " + whereClause);

        IInformationObject[] informationObjects = createQuery(ses, new String[] {Databases.SupplierContact} , whereClause , 1);
        if(informationObjects.length < 1) {return null;}
        return (IDocument) informationObjects[0];
    }
    public static IDocument getContractorFolder(String prjCode, String compCode)  {
        StringBuilder builder = new StringBuilder();
        builder.append("TYPE = '").append(ClassIDs.InvolveParty).append("'")
                .append(" AND ")
                .append("ccmPRJCard_code").append(" = '").append(prjCode).append("'")
                .append(" AND ")
                .append("ObjectNumber").append(" = '").append(compCode).append("'");
        String whereClause = builder.toString();
        System.out.println("Where Clause: " + whereClause);

        IInformationObject[] informationObjects = createQuery(ses, new String[]{Databases.InvolveParty} , whereClause , 1);
        if(informationObjects.length < 1) {return null;}
        return (IDocument) informationObjects[0];
    }
    public static IInformationObject[] createQuery(ISession ses, String[] dbNames , String whereClause , int maxHits){
        String[] databaseNames = dbNames;

        ISerClassFactory fac = ses.getDocumentServer().getClassFactory();
        IQueryParameter que = fac.getQueryParameterInstance(
                ses ,
                databaseNames ,
                fac.getExpressionInstance(whereClause) ,
                null,null);
        if(maxHits > 0) {
            que.setMaxHits(maxHits);
            que.setHitLimit(maxHits + 1);
            que.setHitLimitThreshold(maxHits + 1);
        }
        IDocumentHitList hits = que.getSession() != null? que.getSession().getDocumentServer().query(que, que.getSession()):null;
        if(hits == null) return null;
        else return hits.getInformationObjects();
    }
    public static String getMainCompGVList(ISession ses, String paramName, String infoType) {
        setSes(ses);
        String rtrn = "";
        log.warn("GVList name:" + paramName);
        IStringMatrix settingsMatrix = ses.getDocumentServer().getStringMatrix(paramName, ses);
        log.info("GVList:" + settingsMatrix);
        String rowValuePrjCode = "";
        String rowValueParamCompName = "";
        String rowValueParamCompSName = "";
        String rowValueParamMainComp = "";
        for(int i = 0; i < settingsMatrix.getRowCount(); i++) {
            rowValueParamCompSName = settingsMatrix.getValue(i, 1);
            rowValueParamCompName = settingsMatrix.getValue(i, 2);
            rowValueParamMainComp = settingsMatrix.getValue(i, 7);

            if (!Objects.equals(rowValueParamMainComp, "1")){continue;}

            return (Objects.equals(infoType, "NAME") ? rowValueParamCompName : rowValueParamCompSName);
        }
        return rtrn;
    }
}
