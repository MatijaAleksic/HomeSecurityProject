package backend.admin.keystore;

import backend.admin.DTO.CertificateCreateDTO;
import backend.admin.model.SubjectData;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import java.util.UUID;

@Component
public class Initialize {

    @Autowired
    KeyStoreReader keyStoreReader;

    @Autowired
    KeyStoreWriter keyStoreWriter;

    @Value("${server.ssl.key-alias}")
    private String adminAlias;


    @PostConstruct
    public void init() {
        createCerAndCrlForRootCA();
    }

    public void createCerAndCrlForRootCA() {
        try {
            if (!keyStoreWriter.loadKeyStore())
                createStarterCertificates();
        } catch (OperatorCreationException | CertificateException | CRLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void createStarterCertificates() throws CertificateException, IOException, OperatorCreationException, CRLException {

        keyStoreWriter.createKeyStore();

        CertificateCreateDTO root = new CertificateCreateDTO("admin-backend", "admin", "admin", "Administracija", "Organization Unit", "Country", "admin@gmail.com");
        CertificateCreateDTO frontend = new CertificateCreateDTO("admin-frontend", "frontend", "frontend", "FrontendOrganization", "FrontendUnit", "Serbia", "frontend@gmail.com");

        SubjectData rootSubjectData = generateSubjectData(root);
        SubjectData frontendSubjectData = generateSubjectData(frontend);

        KeyPair keyPairRoot = generateKeyPair();
        KeyPair keyPairFrontend= generateKeyPair();

        assert keyPairRoot != null;
        X509Certificate rootCertificate = generateCertificate(rootSubjectData, keyPairRoot, keyPairRoot.getPrivate(), rootSubjectData.getX500name());

        assert keyPairFrontend != null;
        X509Certificate frontendCertificate = generateCertificate(frontendSubjectData, keyPairFrontend, keyPairRoot.getPrivate(), rootSubjectData.getX500name());

        keyStoreWriter.writeRootCA(root.getCompanyName(), keyPairRoot.getPrivate(), rootCertificate);


        Certificate[] subjectCertificateChain = { frontendCertificate, rootCertificate};
        keyStoreWriter.write(frontend.getCompanyName(), keyPairFrontend.getPrivate(), subjectCertificateChain);

        keyStoreWriter.saveKeyStore();

        createCRL(keyPairRoot.getPrivate(), rootSubjectData.getX500name());
    }

    private X509Certificate generateCertificate(SubjectData subjectData, KeyPair keyPair, PrivateKey issuer, X500Name issuerData) throws OperatorCreationException, CertificateException, CRLException, IOException {

        subjectData.setPublicKey(keyPair.getPublic());
        JcaContentSignerBuilder builder = new JcaContentSignerBuilder("SHA256WithRSAEncryption");
        builder = builder.setProvider("BC");
        ContentSigner contentSigner = builder.build(issuer);

        Date startDate = subjectData.getStartDate();
        Date endDate = subjectData.getEndDate();

        X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(issuerData,
                new BigInteger(subjectData.getSerialNumber()),
                startDate,
                endDate,
                subjectData.getX500name(),
                keyPair.getPublic());


//        GeneralName subjectAltName = new GeneralName(GeneralName.dNSName, "localhost");
        //builder.addOtherName(PKCSObjectIdentifiers.pkcs_9_at_subjectAltName, subjectAltName);
//        certGen.addExtension( Extension.subjectAlternativeName, false, subjectAltName );

        X509CertificateHolder certHolder = certGen.build(contentSigner);

        JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();
        certConverter = certConverter.setProvider("BC");

        X509Certificate createdCertificate = certConverter.getCertificate(certHolder);
        return createdCertificate;
    }

    private void createCRL(PrivateKey pk, X500Name issuerName) throws CRLException, IOException, OperatorCreationException {
        X509v2CRLBuilder crlBuilder = new X509v2CRLBuilder(issuerName, new Date());
        crlBuilder.setNextUpdate(new Date(System.currentTimeMillis() + 86400 * 1000));

        JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder("SHA256WithRSA");
        contentSignerBuilder.setProvider("BC");

        //issuer pk
        X509CRLHolder crlHolder = crlBuilder.build(contentSignerBuilder.build(pk));
        JcaX509CRLConverter converter = new JcaX509CRLConverter();
        converter.setProvider("BC");

        X509CRL crl = converter.getCRL(crlHolder);

        byte[] bytes = crl.getEncoded();


        OutputStream os = new FileOutputStream("src/main/resources/adminCRLs.crl");
        os.write(bytes);
        os.close();
    }

    private SubjectData generateSubjectData(CertificateCreateDTO temp) {

        //Izdaje se sertifikat na dve godine
        Date startDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.YEAR, 3);
        Date endDate = c.getTime();

        Calendar curCal = new GregorianCalendar(TimeZone.getDefault());
        curCal.setTimeInMillis(System.currentTimeMillis());

        String serialNumber = curCal.getTimeInMillis() + "";

        // klasa X500NameBuilder pravi X500Name objekat koji predstavlja podatke o vlasniku
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);

        builder.addRDN(BCStyle.CN, temp.getCompanyName());
        builder.addRDN(BCStyle.SURNAME, temp.getSurName());
        builder.addRDN(BCStyle.GIVENNAME, temp.getGivenName());
        builder.addRDN(BCStyle.O, temp.getOrganization());
        builder.addRDN(BCStyle.OU, temp.getOrganizationUnit());
        builder.addRDN(BCStyle.C, temp.getCountry());
        builder.addRDN(BCStyle.E, temp.getEmail());

        // UID (USER ID) je ID korisnika
        builder.addRDN(BCStyle.UID, System.currentTimeMillis() + "-" + UUID.randomUUID().toString());



        // Kreiraju se podaci za sertifikat, sto ukljucuje:
        // - javni kljuc koji se vezuje za sertifikat
        // - podatke o vlasniku
        // - serijski broj sertifikata
        // - od kada do kada vazi sertifikat
        return new SubjectData(builder.build(), serialNumber, startDate, endDate);

    }

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(2048, random);

            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }

}
