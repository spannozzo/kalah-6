package org.acme.kalah.service;

import org.acme.kalah.entity.DatabaseSequence;
import org.acme.kalah.repository.SequenceGeneratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SequenceGeneratorServiceImpl implements SequenceGeneratorService {

	@Autowired
	SequenceGeneratorRepository sequenceGenaratorRepository;
	
	@Override
	public synchronized long generateSequence(String seqName) {
		
		long[] seq= {0};
		
		sequenceGenaratorRepository.findById(seqName).ifPresentOrElse(x->{
			seq[0]=x.getSeq()+1;
			x.setSeq(seq[0]);
			sequenceGenaratorRepository.save(x);
		
		}, ()->{
			DatabaseSequence dbSequence=new DatabaseSequence();
			
			dbSequence.setId(seqName);
			dbSequence.setSeq(1L);
			
			sequenceGenaratorRepository.save(dbSequence);
			seq[0]=1L;
		});
		
		return seq[0];
		
	}

}
