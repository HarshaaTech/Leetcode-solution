package com.temenos.t24.l3;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.temenos.api.TBoolean;
import com.temenos.api.TDate;
import com.temenos.api.TField;
import com.temenos.api.TString;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;
import com.temenos.t24.api.arrangement.accounting.Contract;
import com.temenos.t24.api.complex.aa.contractapi.BalanceMovement;
import com.temenos.t24.api.complex.aa.contractapi.Payment;
import com.temenos.t24.api.complex.aa.contractapi.RepaymentSchedule;
import com.temenos.t24.api.complex.eb.dataformattingenginehook.DataFormattingContext;
import com.temenos.t24.api.hook.system.DataFormattingEngine;
import com.temenos.t24.api.records.aaaccountdetails.AaAccountDetailsRecord;
import com.temenos.t24.api.records.aaaccountdetails.BillIdClass;
import com.temenos.t24.api.records.aaaccountdetails.BillPayDateClass;
import com.temenos.t24.api.records.aaactivityhistory.AaActivityHistoryRecord;
import com.temenos.t24.api.records.aaactivityhistory.ActivityRefClass;
import com.temenos.t24.api.records.aaactivityhistory.EffectiveDateClass;
import com.temenos.t24.api.records.aaarrangement.AaArrangementRecord;
import com.temenos.t24.api.records.aaarrangement.CustomerClass;
import com.temenos.t24.api.records.aaarrangement.LinkedApplClass;
import com.temenos.t24.api.records.aaarrtermamount.AaArrTermAmountRecord;
import com.temenos.t24.api.records.aabilldetails.AaBillDetailsRecord;
import com.temenos.t24.api.records.aabilldetails.PropertyClass;
import com.temenos.t24.api.records.aaprddesinterest.AaPrdDesInterestRecord;
import com.temenos.t24.api.records.aaprddesinterest.FixedRateClass;
import com.temenos.t24.api.records.aaprddespaymentschedule.AaPrdDesPaymentScheduleRecord;
import com.temenos.t24.api.records.aaprddespaymentschedule.PaymentTypeClass;
import com.temenos.t24.api.records.aaprddespaymentschedule.PercentageClass;
import com.temenos.t24.api.records.aaprddestermamount.AaPrdDesTermAmountRecord;
import com.temenos.t24.api.records.accountclosure.AccountClosureRecord;
import com.temenos.t24.api.records.aclockedevents.AcLockedEventsRecord;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.records.customer.LegalIdClass;
import com.temenos.t24.api.records.dates.DatesRecord;
import com.temenos.t24.api.records.dfemapping.DfeMappingRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Date;
import com.temenos.t24.api.system.Session;


/**
 * Indiv Record Rtn ---This routine will get all the output fields needed in the report
 *
 * @author m.harshaavarthan
 */
public class QbddIndivRecDepInfo extends DataFormattingEngine {

    @Override
    public void updateOutboundData(DataFormattingContext dataFormattingContext, TString data, TBoolean storeData) {

        String dfeparameterId, Delimiter, contractId, legalId, LinkedApplication, arrangementStatus, amount, Paymenttype;
        String Floatingindex, FixedRate, Periodicindex, lockedtype, CapDate, yTerm, lastwrkdate;

        int Count = 0;

        String empty = "";
        String StartDate = "";
        String PaymentFrequency = "";
        String PaymentMethod = "";
        String PrevDate = "";
        String Prevtime = "";
        String ReportEndDate = "";
        String paymentdate = "";

        String recentaclockedid = "";
        Boolean pignoradoFlag = false;
        Boolean lockedeventsFlag = false;
        String pignoradoDesc = "";
        LocalDate FinalReportEndDate = LocalDate.now();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
        String Datemonthyear = "ddMMyyyy";
        String lastWorkingDate = "";
        LocalDate capitaldate = LocalDate.now();
        LocalDate FinalbillDate = LocalDate.now();
        LocalDate LastWorkingDate = LocalDate.now();
        LocalDate datetime = LocalDate.now();
        BigDecimal lockedamount = BigDecimal.ZERO;
        BigDecimal lockedamount1 = BigDecimal.ZERO;
        BigDecimal Embargadosum = BigDecimal.ZERO;
        BigDecimal Fallecidosum = BigDecimal.ZERO;
        BigDecimal Abandonadosum = BigDecimal.ZERO;
        BigDecimal Oposicionsum = BigDecimal.ZERO;
        BigDecimal Pignoradosum = BigDecimal.ZERO;
        BigDecimal AbandonadoEmbargadosum = BigDecimal.ZERO;
        BigDecimal Abandonadotransferirbancocentralsum = BigDecimal.ZERO;
        BigDecimal Totalsumamount = BigDecimal.ZERO;
        BigDecimal yTotalCapAmt = BigDecimal.ZERO;
        BigDecimal yTotalPayAmt = BigDecimal.ZERO;
        Session session = new Session(this);
        String Currency = "";

        String AcLockedEvents = "AC.LOCKED.EVENTS";
        String PropertyOFInterest = "DEPOSITINT";
        String Propertyclassinterest = "INTEREST";
        LegalIdClass LegalObj = new LegalIdClass();
        List<String> acLockedeventsidList = new ArrayList<>();

        DatesRecord dates = new DatesRecord(this);
        AaArrangementRecord yArr = new AaArrangementRecord(this);
        AaActivityHistoryRecord aahis = new AaActivityHistoryRecord(this);
        AaAccountDetailsRecord aadetails = new AaAccountDetailsRecord(this);
        AaBillDetailsRecord aabilldetails = new AaBillDetailsRecord(this);
        CustomerRecord yCus = new CustomerRecord(this);
        AaArrTermAmountRecord aaarrterm = new AaArrTermAmountRecord(this);
        AaPrdDesInterestRecord yAaPrdDesInterestRec = new AaPrdDesInterestRecord(this);
        DataAccess da = new DataAccess(this);
        DfeMappingRecord dfeRecord = new DfeMappingRecord(this);
        CustomerRecord cusrec = new CustomerRecord(this);
        AccountClosureRecord yAcc = new AccountClosureRecord(this);
        AaPrdDesPaymentScheduleRecord yAaPrdDesPaymentRec = new AaPrdDesPaymentScheduleRecord(this);
        Contract aaContract = new Contract(this);
        Payment paymentcls = new Payment();
        AaPrdDesTermAmountRecord yAaPrdDestermamountRec = new AaPrdDesTermAmountRecord(this);
        Logger logger = LoggerFactory.getLogger("API");

        dfeparameterId = dataFormattingContext.getParameterId(); //get the dfeparameterId using the incoming argument.

        logger.debug("DFE.MAPPING ID :" + dfeparameterId);

        try {
            dfeRecord = new DfeMappingRecord(da.getRecord("DFE.MAPPING", dfeparameterId)); // with the id read the table (DfeParameter)
        } catch (Exception e) {
            logger.error("ERROR OPEN DFE.PARAMETER ID :" + e);
        }

        Delimiter = dfeRecord.getFieldDelim().getValue();

        logger.debug("Delimiter:" + Delimiter);
        logger.debug("Incoming values:" + data);

        String data1 = data.toString(); //assign data to string
        //String[] splitter = data1.split(Delimiter);
        contractId = data1;
        logger.debug("CONTRACT ID:" + contractId);
        aaContract.setContractId(contractId);

        try {
            yArr = new AaArrangementRecord(da.getRecord("AA.ARRANGEMENT", contractId)); //get AA record with contract id
        } catch (Exception e) {
            logger.error("Error in reading the record through contratid:" + e);
        }

        arrangementStatus = yArr.getArrStatus().getValue();
        String aclkId = "";
        String cus = "";
        List<CustomerClass> cuslist = yArr.getCustomer();
        for (CustomerClass customerClass : cuslist) {
            String role = customerClass.getCustomerRole().getValue();
            if (role.equals("OWNER")) {
                cus = customerClass.getCustomer().getValue();
            }
        }

        try {
            cusrec = new CustomerRecord(da.getRecord("CUSTOMER", cus)); //get Customer record with the custome rrole and customer of AA record
        } catch (Exception e) {
            logger.error("Error in reading CUSTOMER value:" + e);
        }

        try {
            List<LegalIdClass> legalidList = cusrec.getLegalId();  // From legalidclass get the first value of legal ID
            LegalObj = legalidList.get(0);
            legalId = LegalObj.getLegalId().getValue();
            legalId = legalId.replace("-", "");
        } catch (Exception e) {
            logger.error("NO LEGAL ID : " + cus);
            legalId = "";
        }

        if (legalId == null) {
            legalId = "";
        }

        String[] newData = new String[36]; //Assigning newData to list of array
        for (int i = 0; i < newData.length; i++) {
            if (newData[i] == null) {
                newData[i] = "";                   // Set null value to ""
            }
        }

        newData[0] = legalId;

        List<LinkedApplClass> linkedapp = yArr.getLinkedAppl();
        LinkedApplication = linkedapp.get(0).getLinkedApplId().getValue();

        newData[1] = LinkedApplication;

        logger.debug("CUSTOMER ID value : " + legalId);
        logger.debug("Linkedapplication value : " + LinkedApplication);
        logger.debug("Id of the arrangement 7 :" + contractId);

        String selCmd = "WITH ACCOUNT.NUMBER EQ " + LinkedApplication;  // Selecting the account number with the arrangement account number(linkedapplication)
        try {
            acLockedeventsidList = da.selectRecords("", AcLockedEvents, "", selCmd); // passing the accounts fetched into the arraylist.
        } catch (Exception e) {
            acLockedeventsidList = null;
        }

        logger.debug("acLockedeventsidList value : " + acLockedeventsidList);

        for (String aclocks : acLockedeventsidList) {
            try {
                logger.debug("LOCK ID : " + aclocks);
                AcLockedEventsRecord acRec = new AcLockedEventsRecord(da.getRecord(AcLockedEvents, aclocks)); // with the accounts reading the table aclockedevents
                List<String> datetimeList = acRec.getDateTime();
                String Datetime = datetimeList.get(0);
                String datevalue = Datetime.substring(0, 6); // getting the datevalue which is first 6 (ex-240228)
                int length = Datetime.length();
                String timevalue = Datetime.substring(length - 4, length); // getting the datevalue which is last 4 (ex-1313)

                int value1 = datevalue.compareTo(PrevDate); //comparing previous date to current date
                int value2 = timevalue.compareTo(Prevtime); //comparing previous time to current time

                if (Count == 0) {
                    recentaclockedid = aclocks;//From the selected AC.LOCKED.EVENTS getting the most recently using the counter logics
                } else if (((value1 == 0) && (value2 > 0)) || (value1 > 0)) {
                    recentaclockedid = aclocks; //From the selected AC.LOCKED.EVENTS getting the most recently using the counter logics
                }

                Count += 1;
                if (Count > 0) {
                    PrevDate = datevalue;
                    // store previous date and time value.
                    Prevtime = timevalue;
                }
                logger.debug("recentaclockedidvalue : " + recentaclockedid);

                lockedamount = BigDecimal.valueOf(Double.parseDouble(acRec.getLockedAmount().getValue()));
                lockedtype = acRec.getLockedType().getValue();  //cases//

                switch (lockedtype) {
                    case "EMBARGADO": {
                        Embargadosum = Embargadosum.add(lockedamount);
                        break;
                    }
                    case "FALLECIDO": {
                        Fallecidosum = Fallecidosum.add(lockedamount);
                        break;
                    }
                    case "ABANDONADO": {
                        Abandonadosum = Abandonadosum.add(lockedamount);
                        break;
                    }
                    case "OPOSICION": {
                        Oposicionsum = Oposicionsum.add(lockedamount);
                        break;
                    }
                    case "PIGNORADO": {
                        Pignoradosum = Pignoradosum.add(lockedamount);
                        pignoradoFlag = true;
                        pignoradoDesc = pignoradoDesc + acRec.getDescription();
                        break;
                    }
                    case "ABANDONADO.EMBARGADO": {
                        AbandonadoEmbargadosum = AbandonadoEmbargadosum.add(lockedamount);
                        break;
                    }
                    case "ABANDONADO.TRANSFERIR.BANCOCENTRAL": {
                        Abandonadotransferirbancocentralsum = Abandonadotransferirbancocentralsum.add(lockedamount);
                        break;
                    }
                }
                Totalsumamount = Totalsumamount.add(lockedamount); //all locked amount (not type based)

            } catch (Exception e) {
                logger.error("Error in the values" + e);
            }
        }

        if (pignoradoFlag) {
            logger.error("pignorado " + newData[1]);
            newData[32] = "S";
            newData[33] = pignoradoDesc;
        } else {
            logger.error("No pignorado " + newData[1]);
            newData[32] = "N";
            newData[33] = empty;
        }

        newData[34] = empty;
        newData[35] = "CD";

        newData[5] = Totalsumamount.toString();
        newData[6] = Embargadosum.toString();
        newData[7] = Pignoradosum.toString();
        newData[8] = Oposicionsum.toString();
        newData[9] = Fallecidosum.toString();
        newData[10] = Abandonadosum.toString();
        newData[11] = AbandonadoEmbargadosum.toString();
        newData[12] = Abandonadotransferirbancocentralsum.toString();

        newData[2] = "X";
        if (!recentaclockedid.isEmpty()) {
            try {
                AcLockedEventsRecord acRec = new AcLockedEventsRecord(da.getRecord(AcLockedEvents, recentaclockedid));
                logger.debug("AcLockedEventsRecordvalue: " + acRec);
                String lockedtype1 = acRec.getLockedType().getValue();

                switch (lockedtype1) { //with most rect locked events id executing cases//
                    case "EMBARGADO": {
                        newData[2] = "E";
                        lockedeventsFlag = true;
                        break;
                    }
                    case "FALLECIDO": {
                        newData[2] = "F";
                        lockedeventsFlag = true;
                        break;
                    }
                    case "ABANDONADO": {
                        newData[2] = "A";
                        lockedeventsFlag = true;
                        break;
                    }
                    case "OPOSICION": {
                        newData[2] = "O";
                        lockedeventsFlag = true;
                        break;
                    }
                    case "PIGNORADO": {
                        newData[2] = "P";
                        lockedeventsFlag = true;
                        break;
                    }
                    case "ABANDONADO.EMBARGADO": {
                        newData[2] = "D";
                        lockedeventsFlag = true;
                        break;
                    }
                    case "ABANDONADO.TRANSFERIR.BANCOCENTRAL": {
                        newData[2] = "G";
                        lockedeventsFlag = true;
                        break;
                    }
                }
            } catch (Exception e) {
                logger.error("Error reading the ACLK most recently :" + recentaclockedid);
            }
        }

        try {
            aadetails = new AaAccountDetailsRecord(da.getRecord("AA.ACCOUNT.DETAILS", contractId)); //reading the table with the arrangement id
        } catch (Exception e) {
            logger.error("Error in reading the  AaAccountDetailsrecord value" + e);
        }

        List<TField> Contractdate = aadetails.getContractDate();

        if (!(lockedeventsFlag)) {
            switch (arrangementStatus) {
                case "CURRENT":
                case "EXPIRED": {
                    if (Contractdate.size() == 1) {
                        newData[2] = "V";
                    } else {
                        newData[2] = "R";
                    }
                    break;
                }
                case "PENDING.CLOSURE":
                case "CLOSE": {
                    newData[2] = "C";
                    break;
                }
            }
        }

        List<String> termamountproperty = aaContract.getPropertyIdsForPropertyClass("TERM.AMOUNT");

        try {
            yAaPrdDestermamountRec = new AaPrdDesTermAmountRecord(aaContract.getConditionForProperty(termamountproperty.get(0)));
            amount = yAaPrdDestermamountRec.getAmount().getValue();
            newData[3] = amount;
        } catch (Exception e) {
            logger.error("Error in reading the AaArrTermAmountrecord value" + e);
        }

        try {
            List<BalanceMovement> curBalances = aaContract.getBalanceMovements("CURACCOUNT", ""); //ENQ EB.CONTRACT.BALANCES.BALANCE --Getting the balance type curaccount

            BigDecimal balance;
            BigDecimal balanceBD = new BigDecimal(curBalances.get(curBalances.size() - 1).getBalance().toString());

            balance = balanceBD.subtract(Totalsumamount);

            newData[4] = balance.toString();

            logger.debug(" curBalancesvalue newdata 4: " + newData[4]);
        } catch (Exception e) {
            newData[4] = empty;
            logger.debug(" acdepositintvalue newdata4 catch: " + newData[4]);
        }

        try {
            List<String> propertyInterest = aaContract.getPropertyIdsForPropertyClass(Propertyclassinterest);
            yAaPrdDesInterestRec = new AaPrdDesInterestRecord(aaContract.getConditionForProperty(propertyInterest.get(0)));
            logger.debug(" yAaPrdDesInterestRec value" + yAaPrdDesInterestRec);
            List<FixedRateClass> Fixedratelist = yAaPrdDesInterestRec.getFixedRate();
            String EffectiveRate = Fixedratelist.get(0).getEffectiveRate().toString();
            newData[13] = EffectiveRate;
        } catch (Exception e) {
            logger.error("Error in reading the AaPrdDesInteresrecord value" + e);
        }

        String enddate = aadetails.getReportEndDate().getValue();
        LocalDate reportEndDate = LocalDate.parse(enddate, dtf);
        String reportEndDatefinal = reportEndDate.format(DateTimeFormatter.ofPattern(Datemonthyear));// converting to "dd-MM-yyyy"
        int lastmultivalue = Contractdate.size() - 1;//getting size of the list
        String ContractDates = Contractdate.get(lastmultivalue).getValue();
        logger.debug(" ContractDatesvalue: " + ContractDates);
        LocalDate lastContractDate = LocalDate.parse(ContractDates, dtf);
        String lastContractDateFinal = lastContractDate.format(DateTimeFormatter.ofPattern(Datemonthyear));// converting to "dd-MM-yyyy"
        long daysDifference = ChronoUnit.DAYS.between(reportEndDate, lastContractDate);
        long absoluteDaysDifference = Math.abs(daysDifference);
        yTerm = String.valueOf(absoluteDaysDifference);
        newData[14] = yTerm;

        int TermInMonths = (int) daysDifference;
        int yTermInMonths = Math.abs(TermInMonths / 30); // Absolute value of months
        newData[15] = String.valueOf(yTermInMonths);

        String yStartdate = aadetails.getStartDate().getValue();
        if (yStartdate.equalsIgnoreCase("")) {
            yStartdate = yArr.getStartDate().getValue();
        }
        LocalDate yStartDate = LocalDate.parse(yStartdate, dtf);
        String StartDatefinal = yStartDate.format(DateTimeFormatter.ofPattern(Datemonthyear));// converting to "dd-MM-yyyy"
        newData[21] = StartDatefinal;

        if (arrangementStatus.equals("CURRENT")) {
            ReportEndDate = aadetails.getReportEndDate().getValue();
            FinalReportEndDate = LocalDate.parse(ReportEndDate, dtf);
            String FinalReportEndDatefinal = FinalReportEndDate.format(DateTimeFormatter.ofPattern(Datemonthyear));// converting to "dd-MM-yyyy"
            newData[22] = FinalReportEndDatefinal;
        }

        List<String> property = aaContract.getPropertyIdsForPropertyClass("PAYMENT.SCHEDULE");     // get the property class of the property
        yAaPrdDesPaymentRec = new AaPrdDesPaymentScheduleRecord(aaContract.getConditionForProperty(property.get(0))); //get the first property
        List<PaymentTypeClass> paymenttypelist = yAaPrdDesPaymentRec.getPaymentType();

        for (PaymentTypeClass paymenttype : paymenttypelist) {
            logger.debug("For smallpayment value :" + paymenttype);
            Paymenttype = paymenttype.getPaymentType().getValue();
            if (Paymenttype.equals(Propertyclassinterest)) {
                PaymentFrequency = paymenttype.getPaymentFreq().getValue();
            }
            PaymentMethod = paymenttype.getPaymentMethod().getValue();

            if (PaymentMethod.equals("CAPITALISE")) {
                newData[20] = "R";
            } else {
                newData[20] = empty;
            }
            try {
                List<PercentageClass> PercentageList = paymenttype.getPercentage();
                logger.debug("Percentage value:" + PercentageList);
                for (PercentageClass percentageClass : PercentageList) {
                    StartDate = percentageClass.getStartDate().getValue();
                }
                logger.debug("Percentage valuefinal:" + PercentageList);
                logger.debug("StartDate value:" + StartDate);
            } catch (Exception e) {
                logger.error("NO values in percentageclass:" + e);
            }

            switch (Paymenttype) {  //with payment type executing the cases
                case "INTEREST": {

                    if (StartDate.equals("R_RENEWAL +") || (StartDate.equals("R_RENEWAL")) || (StartDate.equals("R_RENEWAL+"))) {
                        newData[16] = "V";
                    }
                    if (StartDate.equals("R_MATURITY +") || (StartDate.equals("R_MATURITY")) || (StartDate.equals("R_MATURITY+"))) {
                        newData[16] = "V";
                    }

                    if (PaymentFrequency.contains("e1M") && (PaymentMethod.equals("CAPITALISE"))) {
                        newData[16] = "R";
                    }
                    if (PaymentFrequency.contains("e1M") && (PaymentMethod.equals("PAY"))) {
                        newData[16] = "M";
                    }

                    if (PaymentFrequency.contains("e1Y")) {
                        newData[16] = "A";
                    }
                    if (PaymentFrequency.contains("e6M")) {
                        newData[16] = "S";
                    }
                    if (PaymentFrequency.contains("e4M")) {
                        newData[16] = "C";
                    }
                    if (PaymentFrequency.contains("e3M")) {
                        newData[16] = "E";
                    }
                    if (PaymentFrequency.contains("e2M")) {
                        newData[16] = "B";
                    }
                    if (PaymentFrequency.contains("e1M")) {
                        newData[16] = "M";
                    }
                    if (PaymentFrequency.contains("e1W")) {
                        newData[16] = "W";
                    }
                    if (PaymentFrequency.contains("e2W")) {
                        newData[16] = "Q";
                    }
                    if (PaymentFrequency.contains("e1D")) {
                        newData[16] = "D";
                    }
                    break;
                }
                case "INTEREST.ADVANCE": {
                    if (StartDate.contains("R_START")) {
                        newData[16] = "P";
                        break;
                    }
                }
                default: {
                    if (newData[16].isEmpty()) {
                        newData[16] = "X";
                    }
                }
            }

            logger.debug("newdata16 value" + newData[16]);
            logger.debug("newdata16 Paymenttype" + Paymenttype);
            logger.debug("newdata16 PaymentFrequency" + PaymentFrequency);
            logger.debug("newdata16 PaymentMethod" + PaymentMethod);
            logger.debug("newdata16 ContractValue at line 451" + contractId);
            logger.debug("newdata16 logger  after switch 494 value" + newData[16]);
        }

        String MaturityDate = "";
        if (arrangementStatus.equals("CLOSE") || (arrangementStatus.equals("PENDING.CLOSURE"))) {
            yAaPrdDestermamountRec = new AaPrdDesTermAmountRecord(aaContract.getConditionForProperty(termamountproperty.get(0)));
            MaturityDate = yAaPrdDestermamountRec.getMaturityDate().getValue();
            LocalDate FinalMaturityDate = LocalDate.parse(MaturityDate, dtf);

            String FinalMaturityDatefinal = FinalMaturityDate.format(DateTimeFormatter.ofPattern(Datemonthyear));// converting to "dd-MM-yyyy"
            newData[22] = FinalMaturityDatefinal; //check in testing
        }

        try {
            //List<String> PaymentScheduleproperty = aaContract.getPropertyIdsForPropertyClass("PAYMENT.SCHEDULE");//
            if (arrangementStatus.equals("CURRENT")) {
                TDate dateMaturity = new TDate(ReportEndDate);
                String today = session.getCurrentVariable("!TODAY");
                TDate refD = new TDate(today);

                List<RepaymentSchedule> futurePaymentSchedule = aaContract.getRepaymentSchedule(refD, dateMaturity);

                List<String> interestListProp = new ArrayList<>();
                interestListProp.add(Propertyclassinterest);

                paymentdate = getNextDueDates(today, futurePaymentSchedule, interestListProp);

                logger.debug("paymentdatetvalue: " + paymentdate);
            }

            if (paymentdate.isEmpty()) {
                newData[23] = empty;
            } else {
                LocalDate Finalpaymentdate = LocalDate.parse(paymentdate, dtf);
                String Finalpaymentdatevalue = Finalpaymentdate.format(DateTimeFormatter.ofPattern(Datemonthyear));// converting to "dd-MM-yyyy"
                newData[23] = Finalpaymentdatevalue;
            }
        } catch (Exception e) {
            logger.error(" at 527 Error in reading the record value:" + e);
            logger.error("paymentclsvalue: " + paymentcls);
            logger.error("paymentdatetvalue: " + paymentdate);
        }

        try {
            newData[24] = empty;
            if (Contractdate.size() > 1) {
                newData[24] = lastContractDateFinal;
            }
        } catch (Exception e) {
            logger.error("Size of the contract :" + Contractdate.size());
            logger.error("newdata 24 value :" + newData[24]);
            logger.error("If any exceptions :" + e);
        }

        try {
            if (arrangementStatus.equals("CLOSE") || (arrangementStatus.equals("PENDING.CLOSURE"))) {
                ReportEndDate = aadetails.getReportEndDate().getValue();
                FinalReportEndDate = LocalDate.parse(ReportEndDate, dtf);
                String FinalReportEndDatefinal = FinalReportEndDate.format(DateTimeFormatter.ofPattern(Datemonthyear));// converting to "dd-MM-yyyy"
                newData[25] = FinalReportEndDatefinal;
            } else {
                newData[25] = empty;
            }

            Currency = yArr.getCurrency().getValue();

            newData[26] = Currency;

            newData[27] = "221";

            String names = cusrec.getName1().get(0).getValue();
            newData[28] = names + " " + legalId;
        } catch (Exception e) {
            logger.error("exception occured in currency:" + e);
            logger.error("Currency value :" + Currency);
            logger.error("yArr value :" + yArr);
            logger.error("log arrangement 21 :" + contractId);
        }

        List<String> propertyInterest = aaContract.getPropertyIdsForPropertyClass(Propertyclassinterest);    // get the property class of the property
        yAaPrdDesInterestRec = new AaPrdDesInterestRecord(aaContract.getConditionForProperty(PropertyOFInterest));//get the  property
        List<FixedRateClass> fixedratelist = yAaPrdDesInterestRec.getFixedRate();
        try {
            for (FixedRateClass fixedrate : fixedratelist) {
                FixedRate = fixedrate.getFixedRate().getValue();
                Floatingindex = fixedrate.getFloatingIndex().getValue();
                Periodicindex = fixedrate.getPeriodicIndex().getValue();

                if (!(FixedRate.equals(""))) {
                    newData[29] = "N";
                }

                if (!(Floatingindex.equals(""))) {
                    newData[29] = "S";
                }

                if (!(Periodicindex.equals(""))) {
                    newData[29] = "P";
                }
            }
        } catch (Exception e) {
            logger.error("Error in reading the Fixedratevalue" + e);
        }

        newData[30] = empty;
        newData[31] = "1";
        Date date = new Date(this);
        try {
            //List<BalanceMovement> acdepositint = aaContract.getBalanceMovements("ACCDEPOSITINT", "");//ENQ EB.CONTRACT.BALANCES.BALANCE --Getting the balance type curaccount
            String nextWDate = date.getDates().getNextWorkingDay().getValue();
            TDate nextDate = new TDate(subtractDays(nextWDate));
            List<BalanceMovement> acdepositint = aaContract.getBalanceMovementsForPeriod("ACCDEPOSITINT", "TRADE", nextDate, nextDate);
            newData[17] = acdepositint.get(acdepositint.size() - 1).getBalance().toString();  //last multi value of the balance (may be closing balance)
            logger.error(" acdepositintvalue newdata17: " + newData[17]);

        } catch (Exception e) {
            newData[17] = empty;
            logger.error(" acdepositintvalue newdata17 catch: " + newData[17]);
            logger.error("Id of the arrangement 23 :" + contractId);
        }

        try {
            yTotalCapAmt = BigDecimal.valueOf(0);
            yTotalPayAmt = BigDecimal.valueOf(0);
            lastWorkingDate = session.getCurrentVariable("!TODAY");
            //lastWorkingDate = dates.getNextWorkingDay().getValue();
            logger.error("Paso 0 lastWorkingDate: " + lastWorkingDate);
            LastWorkingDate = LocalDate.parse(lastWorkingDate, dtf);

            List<BillPayDateClass> billPayDateList = aadetails.getBillPayDate();
            logger.error("billPayDateListvalue: " + billPayDateList);
            logger.error("Contractid for billvalues: " + contractId);
            for (BillPayDateClass billPayDate : billPayDateList) {
                List<BillIdClass> billIdList = billPayDate.getBillId();
                logger.error("billIdList values: " + billIdList);
                for (BillIdClass billId : billIdList) {
                    String Bills_id = billId.getBillId().getValue();
                    String billdate = billId.getBillDate().getValue();
                    logger.error("Bills_id values: " + Bills_id);
                    logger.error("billdate values: " + billdate);

                    FinalbillDate = LocalDate.parse(billdate, dtf);
                    String billStatus = billId.getPayMethod().getValue();

                    logger.error("payMethod values: " + billStatus);

                    int result = FinalbillDate.compareTo(lastContractDate);
                    int result1 = FinalbillDate.compareTo(LastWorkingDate);

                    if ((result > 0) && (result1 <= 0)) {

                        aabilldetails = new AaBillDetailsRecord(da.getRecord("AA.BILL.DETAILS", Bills_id));

                        if (billStatus.equals("CAPITALISE")) {
                            List<PropertyClass> PropertyList = aabilldetails.getProperty();
                            for (PropertyClass proplist : PropertyList) {
                                String Property = proplist.getProperty().getValue();

                                if (Property.equals(PropertyOFInterest)) {
                                    String orpropamount = proplist.getOrPropAmount().getValue();
                                    yTotalCapAmt = yTotalCapAmt.add(BigDecimal.valueOf(Double.parseDouble(orpropamount)));
                                    logger.debug(" yTotalCapAmt values: " + yTotalCapAmt);
                                }
                                if (Property.equals("DEPOSITINT-TAX")) {
                                    String orpropamount = proplist.getOrPropAmount().getValue();
                                    yTotalCapAmt = yTotalCapAmt.subtract(BigDecimal.valueOf(Double.parseDouble(orpropamount)));
                                    logger.debug(" yTotalCapAmt values: " + yTotalCapAmt);
                                }
                            }
                        }

                        if (billStatus.equals("PAY")) {
                            List<PropertyClass> PropertyList = aabilldetails.getProperty();
                            for (PropertyClass proplist : PropertyList) {
                                String Property = proplist.getProperty().getValue();
                                logger.error("Property values: " + Property);
                                if (Property.equals(PropertyOFInterest)) {
                                    String orpropamount = proplist.getOrPropAmount().getValue();
                                    yTotalPayAmt = yTotalPayAmt.add(BigDecimal.valueOf(Double.parseDouble(orpropamount)));
                                    logger.debug(" yTotalPayAmt values: " + yTotalPayAmt);
                                }
                                if (Property.equals("DEPOSITINT-TAX")) {
                                    String orpropamount = proplist.getOrPropAmount().getValue();
                                    yTotalPayAmt = yTotalPayAmt.subtract(BigDecimal.valueOf(Double.parseDouble(orpropamount)));
                                    logger.debug(" yTotalPayAmt values: " + yTotalPayAmt);
                                }
                            }
                        }
                    }
                }
            }
            newData[18] = String.valueOf(yTotalPayAmt);
            logger.debug("newdata18  value: " + newData[18]);
            newData[19] = String.valueOf(yTotalCapAmt);
            logger.debug("newdata19 value: " + newData[19]);

        } catch (Exception e) {
            logger.error("exception occured in bills :" + e);
            logger.error("Printing AA details :" + aadetails);
            logger.error("arrangement 24 Log :" + contractId);
        }

        try {
            aahis = new AaActivityHistoryRecord(da.getRecord("AA.ACTIVITY.HISTORY", contractId)); //reading the table with the arrangement id
        } catch (Exception e) {
            logger.error("Error in reading the AaActivityHistoryRecord record" + e);
        }

        try {
            List<EffectiveDateClass> effectivedate = aahis.getEffectiveDate(); //getting the effective date from the effectivedateclass
            for (
                    EffectiveDateClass effDate : effectivedate) {
                List<ActivityRefClass> activityref = effDate.getActivityRef();
                for (ActivityRefClass activityRef : activityref) {
                    String Activity = activityRef.getActivity().getValue();
                    String ActivityStatus = activityRef.getActStatus().getValue();
                    if (Activity.equals("DEPOSITS-CHANGE-CUSTOMER") && ActivityStatus.equals("AUTH")) {

                        newData[2] = "T";
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error while accesing history 669" + aahis);
        }

        String dataNew = newData[0];
        int lengthData = newData.length;
        for (int i = 1; i < lengthData; i++) {
            dataNew = dataNew + Delimiter + newData[i];
            logger.debug("Datanewvalue:" + dataNew);
        }

        logger.debug("Final values:" + dataNew);
        data.set(dataNew);

        storeData.set(true);
    }

    public String subtractDays(String dateStr) {
        // Funcion para obtener la fecha maxima
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.parse(dateStr, formatter);
        LocalDate resultDate = date.minusDays(1);

        return resultDate.format(formatter);
    }

    private String getNextDueDates(String today, List<RepaymentSchedule> repaymentsSchedule, List<String> interestPropList) {

        String newDate = "";

        for (RepaymentSchedule repaymentSchedule : repaymentsSchedule) {

            String dueDate = repaymentSchedule.getDueDate().toString();

            if (isDateGreaterThan(dueDate, today)) {

                boolean isAllowed = repaymentSchedule.getRepaymentDueType().stream()
                        .anyMatch(dueType -> interestPropList.contains(dueType.getDueType()));

                if (isAllowed) {
                    return dueDate;
                }
            }
        }

        return newDate;
    }
    private boolean isDateGreaterThan(String lhsDate, String rhsDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate lhsLocalDate = LocalDate.parse(lhsDate, formatter);
        LocalDate rhsLocalDate = LocalDate.parse(rhsDate, formatter);

        return lhsLocalDate.isAfter(rhsLocalDate);
    }
}
