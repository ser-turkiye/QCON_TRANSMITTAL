package utils;

import com.ser.blueline.*;

public class GeneralLib {

   public static ISession ses;

    public static void setSes(ISession ses) {
        GeneralLib.ses = ses;
    }

    public static IDocument getProjectCard(ISession s, String pCode)  {
        setSes(s);

        String objectID ="32e74338-d268-484d-99b0-f90187240549" ;

        StringBuilder builder = new StringBuilder();
        builder.append("TYPE = '").append(objectID).append("'")
                .append(" AND ")
                .append("ccmPRJCard_code").append(" = '").append(pCode).append("'");
        String whereClause = builder.toString();
        System.out.println("Where Clause: " + whereClause);

        IInformationObject[] informationObjects = createQuery(new String[] {"PRJ_FOLDER"} , whereClause , 1);
        if(informationObjects.length < 1) {return null;}
        return (IDocument) informationObjects[0];
    }
    public static IDocument getContactRecord(String eMail)  {
        String SupplierContactWS ="d7ffea9d-3419-4922-8ffa-a0310add5723" ;

        StringBuilder builder = new StringBuilder();
        builder.append("TYPE = '").append(SupplierContactWS).append("'")
                .append(" AND ")
                .append("PrimaryEMail").append(" = '").append(eMail).append("'");
        String whereClause = builder.toString();
        System.out.println("Where Clause: " + whereClause);

        IInformationObject[] informationObjects = createQuery(new String[] {"BPWS"} , whereClause , 1);
        if(informationObjects.length < 1) {return null;}
        return (IDocument) informationObjects[0];
    }
    public static IInformationObject[] createQuery(String[] dbNames , String whereClause , int maxHits){
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


}
