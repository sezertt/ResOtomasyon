package XMLReader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Xml;

import org.apache.http.util.EncodingUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import Entity.Departman;
import Entity.Employee;
import Entity.MasaDizayn;
import Entity.Urunler;

/**
 * Created by Mustafa on 10.6.2014.
 */
public class ReadXML {
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = null;
    Document doc = null;
    public String[] masaPlanIsimleri;
    public ArrayList<Employee> lstEmployees;

    public ArrayList<Departman> readDepartmanlar(List<File> files) {
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        ArrayList<Departman> lstDepartmanlar = new ArrayList<Departman>();
        try {
            for (File file : files) {
                String fileName = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/")
                        + 1);
                if (fileName.contentEquals("restoran.xml")) {
                    doc = dBuilder.parse(file);
                    doc.getDocumentElement().normalize();
                    NodeList nList = doc.getElementsByTagName("Restoran");
                    for (int i = 0; i < nList.getLength(); i++) {
                        Node nNode = nList.item(i);
                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                            Departman d = new Departman();
                            Element element = (Element) nNode;
                            d.DepartmanAdi = element.getElementsByTagName("departmanAdi").item(0).getTextContent();
                            d.DepartmanMenuAdi = element.getElementsByTagName("departmanMenusu").item(0).getTextContent();
                            d.DepartmanEkrani = element.getElementsByTagName("departmanEkrani").item(0).getTextContent();
                            lstDepartmanlar.add(d);
                        }
                    }
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lstDepartmanlar;
    }

    public ArrayList<Urunler> readUrunler(List<File> files) {
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        ArrayList<Urunler> lstUrunler = new ArrayList<Urunler>();
        try {
            for (File file : files) {
                String fileName = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/")
                        + 1);
                if (fileName.contentEquals("urunler.xml")) {
                    doc = dBuilder.parse(file);
                    doc.getDocumentElement().normalize();

                    NodeList nListUrun = doc.getElementsByTagName("KategorilerineGoreUrunler");

                    for (int i = 0; i < nListUrun.getLength(); i++) {
                        Node nNode = nListUrun.item(i);
                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                            Element elementNodeUrunler = (Element) nNode;

                            NodeList nListUrunAdi = ((Element) elementNodeUrunler.getElementsByTagName("urunAdi").item(0)).getElementsByTagName("string");
                            NodeList nListPorsiyonFiyati = ((Element) elementNodeUrunler.getElementsByTagName("porsiyonFiyati").item(0)).getElementsByTagName("string");
                            NodeList nListUrunAciklamasi = ((Element) elementNodeUrunler.getElementsByTagName("urunAciklamasi").item(0)).getElementsByTagName("string");
                            NodeList nListUrunKDV = ((Element) elementNodeUrunler.getElementsByTagName("urunKDV").item(0)).getElementsByTagName("int");

                            for (int k = 0; k < nListUrunAdi.getLength(); k++) {
                                Urunler u = new Urunler();

                                u.urunAdi = nListUrunAdi.item(k).getTextContent().toString();
                                u.porsiyonFiyati = nListPorsiyonFiyati.item(k).getTextContent().toString();
                                u.urunAciklamasi = nListUrunAciklamasi.item(k).getTextContent().toString();
                                u.urunKDV = Integer.parseInt(nListUrunKDV.item(k).getTextContent().toString());
                                u.urunKategorisi = elementNodeUrunler.getElementsByTagName("kategorininAdi").item(0).getTextContent();
                                lstUrunler.add(u);
                            }
                        }
                    }
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lstUrunler;
    }

    public ArrayList<String> readKategoriler(List<File> files) {
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        ArrayList<String> lstKategoriler = new ArrayList<String>();
        try {
            for (File file : files) {
                String fileName = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/")
                        + 1);
                if (fileName.contentEquals("kategoriler.xml")) {
                    doc = dBuilder.parse(file);
                    doc.getDocumentElement().normalize();
                    NodeList nListKategoriler = doc.getElementsByTagName("TumKategoriler");
                    Element elementArrayOfString = (Element) nListKategoriler.item(0);
                    NodeList nListString = elementArrayOfString.getElementsByTagName("string");

                    for (int i = 0; i < nListString.getLength(); i++) {
                        String x = nListString.item(i).getTextContent().toString();
                        lstKategoriler.add(x);
                    }
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lstKategoriler;
    }

    public ArrayList<MasaDizayn> readMasaDizayn(List<File> files) {
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        ArrayList<MasaDizayn> lstMasaDizayn = new ArrayList<MasaDizayn>();
        try {
            for (File file : files) {
                String fileName = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/")
                        + 1);
                if (fileName.contentEquals("masaDizayn.xml")) {
                    doc = dBuilder.parse(file);
                    doc.getDocumentElement().normalize();
                    NodeList nListMasaDizayn = doc.getElementsByTagName("MasaDizayn");
                    String[] masaPlanIsmi = new String[nListMasaDizayn.getLength()];
                    for (int i = 0; i < nListMasaDizayn.getLength(); i++) {
                        Node nNodeMasaDizayn = nListMasaDizayn.item(i);
                        if (nNodeMasaDizayn.getNodeType() == Node.ELEMENT_NODE) {
                            Element elementNodeMasaDizayn = (Element) nNodeMasaDizayn;
                            masaPlanIsmi[i] = elementNodeMasaDizayn.getElementsByTagName("masaPlanIsmi").item(0).getTextContent();
                            NodeList nListArrayOfString = elementNodeMasaDizayn.getElementsByTagName("ArrayOfString");
                            for (int j = 0; j < nListArrayOfString.getLength(); j++) {
                                Node nNodeArrayOfString = nListArrayOfString.item(j);
                                Element elementArrayOfString = (Element) nNodeArrayOfString;
                                NodeList nListString = elementArrayOfString.getElementsByTagName("string");
                                for (int k = 0; k < nListString.getLength(); k++) {
                                    MasaDizayn m = new MasaDizayn();
                                    Node nNodeString = nListString.item(k);
                                    if (!nNodeString.getTextContent().contentEquals("")) {
                                        m.MasaAdi = nNodeString.getTextContent();
                                        m.MasaPlanAdi = elementNodeMasaDizayn.getElementsByTagName("masaPlanIsmi").item(0).getTextContent();
                                        lstMasaDizayn.add(m);
                                    }
                                }
                            }
                        }
                    }
                    masaPlanIsimleri = masaPlanIsmi;
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lstMasaDizayn;
    }

    public String BaseConverter(String text) throws UnsupportedEncodingException {
        byte[] bytes = Base64.decode(text, Base64.DEFAULT);
        String outPut = new String(bytes, "UTF-16LE");
        return outPut;
    }

    public ArrayList<Employee> readEmployees(List<File> files) {
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        lstEmployees = new ArrayList<Employee>();
        try {
            for (File file : files) {
                String fileName = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/")
                        + 1);
                if (fileName.contentEquals("tempfiles.xml")) {
                    doc = dBuilder.parse(file);
                    doc.getDocumentElement().normalize();
                    NodeList nListEmployee = doc.getElementsByTagName("UItemp");

                    for (int i = 0; i < nListEmployee.getLength(); i++) {
                        Employee e = new Employee();
                        Node nNodeEmployee = nListEmployee.item(i);
                        if (nNodeEmployee.getNodeType() == Node.ELEMENT_NODE) {
                            Element elementNodeEmployee = (Element) nNodeEmployee;
                            e.UserName = BaseConverter(elementNodeEmployee.getElementsByTagName("UIUN").item(0)
                                    .getTextContent());
                            e.Name = BaseConverter(elementNodeEmployee.getElementsByTagName("UIN").item(0)
                                    .getTextContent());
                            e.LastName = BaseConverter(elementNodeEmployee.getElementsByTagName("UIS").item
                                    (0).getTextContent());
                            e.PassWord = elementNodeEmployee.getElementsByTagName("UIPW").item(0)
                                    .getTextContent();
                            e.Title = BaseConverter(elementNodeEmployee.getElementsByTagName("UIU").item(0)
                                    .getTextContent());
                            e.PinCode = elementNodeEmployee.getElementsByTagName("UIPN").item
                                    (0).getTextContent();

                            Node nNodeEmpPermissions = elementNodeEmployee.getElementsByTagName("UIY").item
                                    (0);
                            Element elementPermissions = (Element) nNodeEmpPermissions;

                            int PermissionArraySize = elementPermissions.getElementsByTagName("string")
                                    .getLength();
                            String[] Permissions = new String[PermissionArraySize];


                            for (int j = 0; j < PermissionArraySize; j++) {
                                Permissions[j] = elementNodeEmployee.getElementsByTagName("string")
                                        .item(j)
                                        .getTextContent();
                            }
                            e.Permissions = Permissions;
                            lstEmployees.add(e);
                        }
                    }
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lstEmployees;

    }
}
