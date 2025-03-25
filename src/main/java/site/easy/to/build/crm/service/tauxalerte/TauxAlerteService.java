package site.easy.to.build.crm.service.tauxalerte;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import site.easy.to.build.crm.entity.TauxAlerte;
import site.easy.to.build.crm.repository.TauxAlerteRepository;

@Service
public class TauxAlerteService {
    @Autowired
    private TauxAlerteRepository tauxAlerteRepository;

    public BigDecimal getLatestTauxAlerte(){
        return tauxAlerteRepository.findAll().get(0).getAlertPercentage();
    }
}
