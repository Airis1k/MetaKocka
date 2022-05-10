package com.metakocka;

import com.linuxense.javadbf.*;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {

    public static void main(String[] args) throws Throwable {

        FixCertificates.fixUntrustCertificateSSL();

        String url = "https://uloziste.postsignum.cz:44300/";
        String outputPath = "C:\\Users\\Airis\\darbo_failai\\address-rule\\";
        String urlFolder = getLastFolder(url);
        String urlAndFolder = url + urlFolder;

        String[] fileNames = new String[]{"zv_pscad.zip", "zv_pscob.zip", "zv_pscor.zip", "zv_pscpo.zip", "zv_pscu1.zip"};

        // downloads to memory, extracts from memory and saves .tsv files in disk

        // TODO init merger list
        List<AddressRule> addressRuleMerged = new ArrayList<>();

        for (String fileName : fileNames) {
            System.out.println(fileName);
            ByteArrayInputStream bais = downloadFileToMemory(urlAndFolder, fileName);
            // choose only readDbfFile or downloadAndParseOneFile method
//            readDbfFile(bais, fileName);
            addressRuleMerged.addAll(downloadAndParseOneFile(outputPath, bais, fileName));
        }

        // TODO filtering
        List<AddressRule> listedAddressRuleUnique = new ArrayList<AddressRule>();
        Set<String> keySet = new HashSet<>();
        for (AddressRule ae : addressRuleMerged) {
            String key = ae.getCity() + "#" + ae.getPost_number();
            if (keySet.contains(key)) {
                continue;
            }
            listedAddressRuleUnique.add(ae);
            keySet.add(key);
        }

        // TODO writing to disk
        FileOutputStream fs = new FileOutputStream("C:\\Users\\Airis\\darbo_failai\\address-rule\\files.tsv");
        fs.write("NAZOBCE\tPSC".getBytes());
        for (AddressRule ae : listedAddressRuleUnique) {
            if (ae.getPost_number() != null && ae.getCity() != null && !ae.getPost_number().trim().equals("") && !ae.getCity().trim().equals("")) {     // trim() - pasalina space'us ir simbolius
                fs.write("\n".getBytes());
                fs.write(ae.getCity().getBytes());
                fs.write("\t".getBytes());
                fs.write(ae.getPost_number().getBytes());
            }
        }

        // Connection to DB
        Reader reader = Resources.getResourceAsReader("SqlMapConfig.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        SqlSession session = sqlSessionFactory.openSession();

        // Object
        session.delete("AddressRule.delete");
        System.out.println("Records deleted successfully!");
        for (AddressRule list : listedAddressRuleUnique) {
            if (list.getPost_number() != null && list.getCity() != null && !list.getPost_number().trim().equals("") && !list.getCity().trim().equals("")) {
                AddressRule addressRule = new AddressRule(null, "Region", list.getCity(), "Kauno g.", list.getPost_number(), "validation_type", "prefix", false, false);

                // Insert student data
                session.insert("AddressRule.insert", addressRule);
                System.out.println("Record inserted successfully!");
                session.commit();
            }
        }

        session.close();
    }

    public static ByteArrayInputStream downloadFileToMemory(String urlAndFolder, String fileName) throws IOException {
        InputStream inputStream = null;

        ByteArrayInputStream bais = null;
        try {
            // Create a URL object
            URL url = new URL(urlAndFolder + fileName);

            // authentification to log in
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("ZV03997000", "4.s,3Kj/VG".toCharArray());
                }
            });

            // Connection to the file
            URLConnection urlConnection = url.openConnection();

            // ByteArray
            ByteArrayOutputStream bao = new ByteArrayOutputStream();

            // Get input stream to the file
            inputStream = urlConnection.getInputStream();

            final byte[] b = new byte[2048];
            int length;
            // read from input stream and write to output stream
            while ((length = inputStream.read(b)) != -1) {
                // bao || outputStream
                bao.write(b, 0, length);
            }

            byte[] data = bao.toByteArray();

            ByteArrayInputStream bin = new ByteArrayInputStream(data);

            ByteArrayOutputStream bao2 = new ByteArrayOutputStream();

            ZipInputStream zipStream = new ZipInputStream(bin);
            // entry - one file in zip archive
            ZipEntry entry;
            // block of computer memory that serves as a temporary placeholder
            byte[] buffer = new byte[2048];

            while ((entry = zipStream.getNextEntry()) != null) {
                // pasiema zip faila ir ji dalimis(buffer dydzio) raso i output faila
                int len;
                while ((len = zipStream.read(buffer)) > 0) {
                    bao2.write(buffer, 0, len);
                }

                byte[] data2 = bao2.toByteArray();

                ByteArrayInputStream bin2 = new ByteArrayInputStream(data2);
                System.out.println(fileName.replace("zip","dbf") + " has been successfully saved to memory.");

                bais = bin2;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        } finally {
            // Close streams
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return bais;
    }

    public static void readDbfFile(ByteArrayInputStream bais, String fileName) {
        DBFReader reader = null;

        try {
            reader = new DBFReader(bais);

            DBFRow row;

            int numberOfFields = reader.getFieldCount();
            Boolean PSCexists = false;
            Boolean NAZOBCEexists = false;

            for (int i = 0; i < numberOfFields; i++) {
                DBFField field = reader.getField(i);

                if (field.getName().equals("PSC")) {
                    PSCexists = true;
                }
                if (field.getName().equals("NAZOBCE")) {
                    NAZOBCEexists = true;
                }
            }

//            for (int i = 0; i < (reader.getRecordCount() - 2); i++) Used for print all info
            // Below loop prints first 100 records for every file
            for (int i = 0; i < (Math.min(reader.getRecordCount() - 2, 100)) ; i++) {
                row = reader.nextRow();

                if (NAZOBCEexists) {
                    System.out.print(row.getString("NAZOBCE"));
                    System.out.print(" ");
                }

                if (PSCexists) {
                    if (!row.getString("PSC").equals("PSC")) {
                        System.out.println(row.getString("PSC"));
                    }
                } else if (NAZOBCEexists) {
                    System.out.println();
                }
            }
        } catch (DBFException e) {
            e.printStackTrace();
        } finally {
            DBFUtils.close(reader);
        }
    }

    public static String getLastFolder(String urlAndFolder) {
        String latestFolder = "";

        try {
            URL url = new URL(urlAndFolder);
            URLConnection urlConnection = url.openConnection();

            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("ZV03997000", "4.s,3Kj/VG".toCharArray());
                }
            });

            Document doc = Jsoup.connect(urlAndFolder).get();
            Elements folders = doc.select("td");
            Elements pag = folders.select("a");

            String idk = pag.html();

            String folder = idk.replace("_","");
            folder = folder.replace("/","");

            String swap = null;
            for (int i = 0; i < 2; i=i+6) {
                if (i == 0) {
                    swap = folder.substring(i+2,i+6) + folder.substring(i,i+2);
                }
                for (int j=6; j<folder.length(); j=j+7) {
                    swap = folder.substring(j+3,j+7) + folder.substring(j+1,j+3);
                }
            }

            latestFolder = swap.substring(4,+6) + "_" + swap.substring(0,4) + "/";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return latestFolder;
    }

    public static List<AddressRule> downloadAndParseOneFile(String outputPath, ByteArrayInputStream bais, String fileName) {
        List<AddressRule> listedAddressRule = new ArrayList<AddressRule>();

        DBFReader reader = null;

        try {
            File theDir = new File(outputPath);
            if (!theDir.exists()) {
                theDir.mkdirs();
            }

            reader = new DBFReader(bais);

            DBFRow row;

            int numberOfFields = reader.getFieldCount();
            Boolean PSCexists = false;
            Boolean NAZOBCEexists = false;

            for (int i = 0; i < numberOfFields; i++) {
                DBFField field = reader.getField(i);

                if (field.getName().equals("PSC")) {
                    PSCexists = true;
                }
                if (field.getName().equals("NAZOBCE")) {
                    NAZOBCEexists = true;
                }
            }

//            for (int i = 0; i < (reader.getRecordCount() - 2); i++) Used for print all info
            // Below loop prints first 100 records for every file
            for (int i = 0; i < (reader.getRecordCount() - 2); i++) {
                row = reader.nextRow();
                AddressRule addressRule = new AddressRule();

                if (NAZOBCEexists) {
                    addressRule.setCity(row.getString("NAZOBCE"));
                    listedAddressRule.add(addressRule);
                }
                if (PSCexists) {
                    addressRule.setPost_number(row.getString("PSC"));
                    listedAddressRule.add(addressRule);
                }
            }

            return listedAddressRule;
        } catch (DBFException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            DBFUtils.close(reader);
        }
    }
}