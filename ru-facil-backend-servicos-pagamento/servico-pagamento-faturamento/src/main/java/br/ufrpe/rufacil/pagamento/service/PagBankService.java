package br.ufrpe.rufacil.pagamento.service;

import br.ufrpe.rufacil.pagamento.model.PagBankResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

/**
 * Simula a integração com a API PagBank (Orders v4) e gera o QR Code
 * localmente usando a biblioteca ZXing — sem depender de API externa.
 *
 * A imagem é retornada como Base64 (data:image/png;base64,...) e pode
 * ser exibida diretamente numa tag <img> no frontend.
 */
@Service
public class PagBankService {

    /**
     * Processa o pagamento (mock PagBank) e gera QR Code via ZXing.
     *
     * @param cpf           CPF do aluno pagante
     * @param valorRefeicao Valor a cobrar (ex: "3.00")
     * @param transacaoId   ID único gerado pelo controller
     * @return PagBankResponse com código Pix e QR Code em Base64
     */
    public PagBankResponse processarPagamento(String cpf, String valorRefeicao, String transacaoId) {
        String pixCopiaECola = gerarPixMock(cpf, valorRefeicao, transacaoId);
        String qrCodeBase64 = gerarQrCodeBase64(pixCopiaECola);

        return new PagBankResponse(
                transacaoId,
                "AGUARDANDO_PAGAMENTO",
                pixCopiaECola,
                qrCodeBase64
        );
    }

    /**
     * Gera código Pix Copia e Cola no formato EMV/BR Code (padrão BACEN).
     */
    private String gerarPixMock(String cpf, String valor, String txId) {
        String chavePix       = "rufacil@ufrpe.br";
        String nomeBeneficiario = "RU FACIL UFRPE";
        String cidade         = "RECIFE";
        String txIdLimpo = txId.replaceAll("[^A-Za-z0-9]", "");
        txIdLimpo = txIdLimpo.substring(0, Math.min(txIdLimpo.length(), 25));

        String merchantInfo = "0014BR.GOV.BCB.PIX0117" + chavePix;
        String additionalData = "05" + String.format("%02d", txIdLimpo.length()) + txIdLimpo;
        String additionalDataField = "62" + String.format("%02d", additionalData.length()) + additionalData;

        String payload =
                "000201"
                + "010212"
                + "26" + String.format("%02d", merchantInfo.length()) + merchantInfo
                + "52040000"
                + "5303986"
                + "54" + String.format("%02d", valor.length()) + valor
                + "5802BR"
                + "59" + String.format("%02d", nomeBeneficiario.length()) + nomeBeneficiario
                + "60" + String.format("%02d", cidade.length()) + cidade
                + additionalDataField
                + "6304";

        // CRC16-CCITT real (exigido pelo BACEN para o código ser válido)
        String crc = calcularCrc16(payload);
        return payload + crc;
    }

    /**
     * Calcula o CRC16-CCITT (polinômio 0x1021) usado no padrão BR Code do BACEN.
     */
    private String calcularCrc16(String payload) {
        int crc = 0xFFFF;
        for (char c : payload.toCharArray()) {
            crc ^= (c << 8);
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
                crc &= 0xFFFF;
            }
        }
        return String.format("%04X", crc);
    }

    /**
     * Gera o QR Code como string Base64 (PNG 300x300) usando ZXing.
     * O resultado pode ser usado diretamente como src de uma tag <img>:
     *   <img src="data:image/png;base64,{valor}" />
     */
    private String gerarQrCodeBase64(String conteudo) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.CHARACTER_SET, "UTF-8",
                    EncodeHintType.MARGIN, 2
            );
            BitMatrix matrix = writer.encode(conteudo, BarcodeFormat.QR_CODE, 300, 300, hints);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Erro ao gerar QR Code: " + e.getMessage(), e);
        }
    }
}
