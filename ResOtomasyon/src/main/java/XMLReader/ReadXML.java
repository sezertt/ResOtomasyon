package XMLReader;

import android.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import Entity.Departman;
import Entity.Employee;
import Entity.MasaDizayn;
import Entity.Menu;
import Entity.UrunlerinListesi;

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

    public ArrayList<UrunlerinListesi> readUrunler(List<File> files) {
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        ArrayList<UrunlerinListesi> lstUrunler = new ArrayList<UrunlerinListesi>();
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
                            NodeList nListPorsiyonFiyati = ((Element) elementNodeUrunler.getElementsByTagName("urunPorsiyonFiyati").item(0)).getElementsByTagName("string");
                            NodeList nListUrunAciklamasi = ((Element) elementNodeUrunler.getElementsByTagName("urunAciklamasi").item(0)).getElementsByTagName("string");
                            NodeList nListUrunKDV = ((Element) elementNodeUrunler.getElementsByTagName("urunKDV").item(0)).getElementsByTagName("int");
                            NodeList nListUrunPorsiyonu = ((Element) elementNodeUrunler.getElementsByTagName("urunPorsiyonSinifi").item(0)).getElementsByTagName("int");
                            NodeList nListUrunTuru = ((Element) elementNodeUrunler.getElementsByTagName("urunTuru").item(0)).getElementsByTagName("string");

                            for (int k = 0; k < nListUrunAdi.getLength(); k++) {
                                // sadece kilogramla satılan ürünler tablette görünmemeli, kilogram ve porsiyon olan ürünler sadece porsiyon olarak satılacaktır ve görünecektir
                                if(nListUrunTuru.item(k).getTextContent().contentEquals("Kilogram"))
                                    continue;

                                UrunlerinListesi u = new UrunlerinListesi();

                                u.urunAdi = nListUrunAdi.item(k).getTextContent();
                                u.urunFiyati = nListPorsiyonFiyati.item(k).getTextContent();
                                u.urunAciklamasi = nListUrunAciklamasi.item(k).getTextContent();
                                u.urunKDV = Integer.parseInt(nListUrunKDV.item(k).getTextContent());
                                u.urunPorsiyonSinifi = Double.parseDouble(nListUrunPorsiyonu.item(k).getTextContent());
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

    public ArrayList<Menu> readMenuler(List<File> files) {
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        ArrayList<Menu> lstMenuler = new ArrayList<Menu>();
        try {
            for (File file : files) {
                String fileName = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/")
                        + 1);
                if (fileName.contentEquals("menu.xml")) {
                    doc = dBuilder.parse(file);
                    doc.getDocumentElement().normalize();
                    NodeList nListMenuler = doc.getElementsByTagName("Menuler");
                    for (int i = 0; i < nListMenuler.getLength(); i++) {
                        Node nNode = nListMenuler.item(i);
                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                            Menu menu = new Menu();
                            Element elementMenuler = (Element) nNode;
                            NodeList nListMenununKategorileri = ((Element) elementMenuler.getElementsByTagName("menukategorileri").item(0)).getElementsByTagName("string");

                            menu.MenununKategorileri = new ArrayList<String>();

                            for (int k = 0; k < nListMenununKategorileri.getLength(); k++) {
                                menu.MenununKategorileri.add(nListMenununKategorileri.item(k).getTextContent());
                            }
                            menu.MenuAdi = elementMenuler.getElementsByTagName("menuAdi").item(0).getTextContent();

                            lstMenuler.add(menu);
                        }
                    }
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lstMenuler;
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
                                        m.MasaEkraniAdi = elementNodeMasaDizayn.getElementsByTagName("masaPlanIsmi").item(0).getTextContent();
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
        return new String(bytes, "UTF-16LE");
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
