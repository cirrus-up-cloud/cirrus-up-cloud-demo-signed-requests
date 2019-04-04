package cloud.cirrusup.filter;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.tomitribe.auth.signatures.AuthenticationException;
import org.tomitribe.auth.signatures.Signature;
import org.tomitribe.auth.signatures.Signer;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Filter used to validate HTTP signature.
 */
@Configuration
public class HTTPSignatureCheckerFilter implements Filter {

    /**
     * Class logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(HTTPSignatureCheckerFilter.class);

    private static final String AUTHORIZATION = "Authorization";

    @Value("${filters.signing.key}")
    private String key;

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        if (!isValidRequestHeader(req) || !checkHeaderValueIsValid((HttpServletRequest) req)) {

            res.resetBuffer();
            res.getOutputStream().write("Authentication issue: Authorization header is missing or is not a valid signature.".getBytes());
            ((HttpServletResponse) res).setStatus(401);
        } else {

            chain.doFilter(req, res);
        }
    }

    private boolean checkHeaderValueIsValid(HttpServletRequest req) {

        try {

            Signature signature = Signature.fromString(req.getHeader(AUTHORIZATION));

            List<String> headerList = signature.getHeaders();
            Map<String, String> headersVal = Maps.newHashMap();
            for (String header : headerList) {
                String value = req.getHeader(header);
                if (value == null) {
                    LOG.warn("Missing header {}.", header);
                    return false;
                }
                headersVal.put(header, value);
            }
            Signer signer = new Signer(new SecretKeySpec(key.getBytes(), signature.getAlgorithm().getPortableName()), signature);
            Signature sign = signer.sign(req.getMethod(), req.getRequestURI(), headersVal);
            return sign.getSignature().equals(signature.getSignature());

        } catch (AuthenticationException | IOException e) {

            LOG.warn("Exception on validating signature ", e);
            return false;
        }
    }

    private boolean isValidRequestHeader(ServletRequest req) {

        return req != null && req instanceof HttpServletRequest
                && StringUtils.isNotEmpty(((HttpServletRequest) req).getHeader(AUTHORIZATION));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }
}
