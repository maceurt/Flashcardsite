package com.flashcardapp.flashcard.controllers;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


import com.flashcardapp.flashcard.repositories.UserRepository;
import com.flashcardapp.flashcard.repositories.FlashcardRepository;
import com.flashcardapp.flashcard.ent.User;
import com.flashcardapp.flashcard.ent.Flashcardset;


//Controller for ajax requests
@Controller
@RequestMapping("/flashcard/ajax")
public class AjaxController {

    //Repositories to allow querying of user and flashcard repositories.
    private UserRepository userRepo;
    private FlashcardRepository flashcardRepo;

    //Arguments get automatically wired into controller by spring.
    public AjaxController(UserRepository userRepo, FlashcardRepository flashcardRepo) {
        this.userRepo = userRepo;
        this.flashcardRepo = flashcardRepo;
    }

    @RequestMapping(value = "/Flashcardset/delete", method = RequestMethod.POST, produces = "text/plain")
    public @ResponseBody String deleteFlashcardset(@RequestParam("id") String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Flashcardset fcs = this.flashcardRepo.findOne(id);

        //Checks to make sure that the flashcardset with that id actually exists.
        if (fcs == null) {
            return "Flashset with that id not found.";
        }
        else if (!fcs.getOwner().equals(auth.getName())) {
            return "You are not the owner of that flashset.";
        }
        else {
            User ln_user = this.userRepo.findOne(auth.getName());
            this.flashcardRepo.delete(id);
            ln_user.removeFlashset(id);

            //Saves user into database with updated flashcardset.
            this.userRepo.updateUserDetails(ln_user);
            return "Success";
        }
    }

    @RequestMapping(value = "/Flashcardset", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody Flashcardset getFlashcardset(@RequestParam("id") String id) {
        Flashcardset fcs = this.flashcardRepo.findOne(id);
        return fcs;
    }

    @RequestMapping(value = "/Flashcardset/copy", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody Flashcardset copyFlashcardset(@RequestParam("id") String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        User ln_user = this.userRepo.findOne(auth.getName());
        Flashcardset fcs = this.flashcardRepo.findOne(id);

        Flashcardset copied_set = new Flashcardset(this.flashcardRepo.makeRandomId(), fcs.getName() + " [Copy]", fcs.getFlashcardsStr(), auth.getName());
        this.flashcardRepo.save(copied_set);
        ln_user.addFlashset(copied_set.getId());
        this.userRepo.updateUserDetails(ln_user);

        return copied_set;
    }


    @RequestMapping(value = "/Flashcardset/updateCards", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody Flashcardset updateCardsFlashcardset(@RequestParam("id") String id, @RequestParam("flashcards") String flashcards) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Flashcardset fcs = this.flashcardRepo.findOne(id);

        //Check that owner of set sent request.
        if (!fcs.getOwner().equals(auth.getName())) {
            return null;
        }
        else {
            fcs.setFlashcardsFromStr(flashcards);
            this.flashcardRepo.updateFlashcardset(fcs);
            return fcs;
        }
    }

    @RequestMapping(value = "/Flashcardset/addCard", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody Flashcardset addCardFlashcardset(@RequestParam("id") String id, @RequestParam("front") String frontF, @RequestParam("back") String backF) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Flashcardset fcs = this.flashcardRepo.findOne(id);

        //Check that owner of set sent request.
        if (!fcs.getOwner().equals(auth.getName())) {
            return null;
        }
        else {
            fcs.addToFlashcards(frontF + "\\" + backF);
            this.flashcardRepo.updateFlashcardset(fcs);
            return fcs;
        }
    }

}
