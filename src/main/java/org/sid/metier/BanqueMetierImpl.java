package org.sid.metier;

import java.util.Date;

import org.sid.dao.CompteRepository;
import org.sid.dao.OperationRepository;
import org.sid.entities.Compte;
import org.sid.entities.CompteCourant;
import org.sid.entities.Operation;
import org.sid.entities.Retrait;
import org.sid.entities.Versement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BanqueMetierImpl implements IBanqueMetier {
	@Autowired
	private CompteRepository compteRespository;
	@Autowired
	private OperationRepository operationRespository;
	
	@Override
	public Compte consulterCompte(String codeCpte) {
		Compte cp = compteRespository.findById(codeCpte).get();
		if(cp == null) throw new RuntimeException("Compte introuvable");
		return cp;
	}

	@Override
	public void verser(String codeCpte, double montant) {
		Compte cp = consulterCompte(codeCpte);
		Versement v = new Versement(new Date(), montant, cp);
		operationRespository.save(v);
		cp.setSolde(cp.getSolde()+montant);
		compteRespository.save(cp);
	}

	@Override
	public void retirer(String codeCpte, double montant) {
		Compte cp = consulterCompte(codeCpte);
		double facilitesCaisse = 0;
		if(cp instanceof CompteCourant)
			facilitesCaisse = ((CompteCourant) cp).getDecouvert();
		if(cp.getSolde()+facilitesCaisse<montant)
			throw new RuntimeException("Solde insuffisant");
		Retrait v = new Retrait(new Date(), montant, cp);
		operationRespository.save(v);
		cp.setSolde(cp.getSolde()-montant);
		compteRespository.save(cp);

	}

	@Override
	public void virement(String codeCpte1, String codeCpte2, double montant) {
		if(codeCpte1.equals(codeCpte2)) {
			throw new RuntimeException("Impossible d'effectuer le virement, c'est le même compte");
		}
		retirer(codeCpte1, montant);
		verser(codeCpte2, montant);

	}

	@Override
	public Page<Operation> listOperation(String codeCpte, int page, int size) {
		
		return operationRespository.listOperation(codeCpte, PageRequest.of(page, size));
	}

}
