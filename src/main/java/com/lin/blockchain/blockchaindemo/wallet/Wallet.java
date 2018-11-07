package com.lin.blockchain.blockchaindemo.wallet;

import lombok.Data;
import util.Base58Check;
import util.BtcAddressUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.ECGenParameterSpec;

/**
 * 账户(模拟)
 * 相当于在区块链世界的身份证
 *      用户公钥：相当于是对外提供的银行卡号，用于提供交易地址
 *      用户秘钥：想当于密码，用于验证此次交易是否是由本人操作
 */
@Data
public class Wallet {
    /** 自增Id */
    private Integer id;
    /** 用户公钥 */
    private PublicKey publicKey;
    /** 用户秘钥 */
    private PrivateKey privateKey;

    public Wallet (){
        generateKeyPair();
    }

    public void generateKeyPair() {
        try {
            //ECDSA--椭圆曲线算法获得秘钥，使用 BC(BouncyCastleProvider) Provider
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            // 椭圆曲线（EC）域参数设定
            // bitcoin 为什么会选择 secp256k1，详见：https://bitcointalk.org/index.php?topic=151120.0
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
            // Initialize the key generator and generate a KeyPair
            keyGen.initialize(ecSpec, random); //256
            KeyPair keyPair = keyGen.generateKeyPair();
            // Set the public and private keys from the keyPair
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();

        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取钱包地址(version(版本号) + paylod(公钥) + checksum(校验码))
     *
     * @return
     */
    public String getAddress() {
        try {
            // 1. 获取 ripemdHashedKey
            byte[] ripemdHashedKey = BtcAddressUtils.ripeMD160Hash(this.getPublicKey().toString().getBytes());

            // 2. 添加版本 0x00
            ByteArrayOutputStream addrStream = new ByteArrayOutputStream();
            addrStream.write((byte) 0);
            addrStream.write(ripemdHashedKey);
            byte[] versionedPayload = addrStream.toByteArray();

            // 3. 计算校验码
            byte[] checksum = BtcAddressUtils.checksum(versionedPayload);

            // 4. 得到 version + paylod + checksum 的组合
            addrStream.write(checksum);
            byte[] binaryAddress = addrStream.toByteArray();

            // 5. 执行Base58转换处理
            return Base58Check.rawBytesToBase58(binaryAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Fail to get wallet address ! ");
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }
}
